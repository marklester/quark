package quark.populator;

import java.util.concurrent.CountDownLatch;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quark.CurrencyManager;
import quark.MarketManager;
import quark.TradePairManager;
import quark.db.DatabaseManager;
import quark.db.PostgresDatabaseManager;
import quark.model.CurrencyLookup;

public class MarketPopulator {
  private static final Logger LOGGER = LoggerFactory.getLogger(MarketPopulator.class);
  public static final String GROUP = "quark";
  
  private Scheduler scheduler;

  private DatabaseManager dbManager;
  private MarketManager mktManager;

  public MarketPopulator(DatabaseManager dbManager, MarketManager mktManager)
      throws SchedulerException {
    this.dbManager = dbManager;
    this.mktManager = mktManager;
    scheduler = StdSchedulerFactory.getDefaultScheduler();
  }

  public void startPolling() {
    JobKey key = JobKey.jobKey("retrieve-markets-job",GROUP);
    TriggerKey triggerKey = TriggerKey.triggerKey("hourly-trigger");

    JobDataMap map = new JobDataMap();
    map.put(RetrieveMarketsJob.ORDER_DAO, dbManager.getOrderDao());
    map.put(RetrieveMarketsJob.MKT_MANAGER, mktManager);

    JobDetail job =
        JobBuilder.newJob(RetrieveMarketsJob.class).withIdentity(key).usingJobData(map).build();
    Trigger trigger = TriggerBuilder.newTrigger().startNow().withIdentity(triggerKey)
        .withSchedule(SimpleScheduleBuilder.repeatHourlyForever()).build();

    try {
      scheduler.start();
      scheduler.scheduleJob(job, trigger);
    } catch (SchedulerException e) {
      LOGGER.error("scheduling job failed", e);
    }
  }

  public static void main(String args[]) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    DatabaseManager dbManager = new PostgresDatabaseManager();
    CurrencyLookup lookup = CurrencyLookup.create();
    CurrencyManager currencyManager = new CurrencyManager(lookup);
    TradePairManager tradePairManager = TradePairManager.create(currencyManager);
    MarketManager marketManager = new MarketManager(tradePairManager);
    MarketPopulator fullMarketHistory = new MarketPopulator(dbManager, marketManager);
    fullMarketHistory.startPolling();
    latch.await();
  }
}
