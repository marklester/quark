package quark.populator;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import quark.MarketManager;
import quark.db.OrderDAO;
import quark.model.Market;

public class RetrieveMarketsJob implements Job {
  private static final Logger LOGGER = LoggerFactory.getLogger(RetrieveMarketsJob.class);
  public static final String ORDER_DAO = "database-manager";
  public static final String MKT_MANAGER = "market-manager";
  public static final String CRON = "* 0/15 * ? * *";

  public RetrieveMarketsJob() {}

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    MarketManager mktManager =
        (MarketManager) context.getJobDetail().getJobDataMap().get(MKT_MANAGER);
    OrderDAO orderDao = (OrderDAO) context.getJobDetail().getJobDataMap().get(ORDER_DAO);
    LocalDateTime lastOrder = orderDao.getLastOrderDate();
    LOGGER.info("Getting orders after: " + lastOrder);


    Stopwatch total = Stopwatch.createStarted();
    try {
      Collection<Market> markets = mktManager.getMarkets();
      int count = 0;
      for (Market market : markets) {
        JobKey key = JobKey.jobKey(market.getLabel(),MarketPopulator.GROUP);
        Trigger trigger =
            TriggerBuilder.newTrigger().withIdentity(TriggerKey.triggerKey(market.getLabel(),MarketPopulator.GROUP)).startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(CRON)).build();
        JobDataMap map = new JobDataMap();
        map.put(RetrieveMarketsJob.ORDER_DAO, orderDao);
        map.put(RetrieveMarketHistoryJob.MARKET, market);
        map.put(RetrieveMarketHistoryJob.POSITION, new Position(count, markets.size()));
        JobDetail job = JobBuilder.newJob(RetrieveMarketHistoryJob.class).withIdentity(key)
            .usingJobData(map).build();

        try {
          context.getScheduler().scheduleJob(job, trigger);
        } catch (SchedulerException e) {
          LOGGER.error("could not schedule job for {}", market, e);
        }
        count += 1;
      }
      LOGGER.info("took {}", total.stop());
    } catch (ExecutionException e1) {
      LOGGER.error("could not get markets",e1);
    }

  }
}