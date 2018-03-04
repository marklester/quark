package quark.populator;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;

import quark.CryptopiaGetter;
import quark.db.OrderDAO;
import quark.model.Market;
import quark.orders.Order;
import quark.orders.StandardOrder;

public class RetrieveMarketHistoryJob implements Job {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final Logger LOGGER = LoggerFactory.getLogger(RetrieveMarketHistoryJob.class);
  public static final String MARKET = "market";
  public static final String POSITION = "position";

  public RetrieveMarketHistoryJob() {}

  public Set<Order> createMarketHistory(long tradePairId, LocalDateTime startPoint)
      throws Exception {
    LocalDateTime startPoint2 = MoreObjects.firstNonNull(startPoint, LocalDateTime.MIN);
    String getHistoryUrl =
        CryptopiaGetter.BASE_CRYPTOPIA_API_URL + "GetMarketHistory/" + tradePairId + "/48";
    HttpClient client = HttpClientBuilder.create()
        .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
        .build();
    HttpGet get = new HttpGet(getHistoryUrl);
    HttpResponse response = client.execute(get);
    JsonNode jsonNode = MAPPER.readTree(response.getEntity().getContent());
    return StreamSupport.stream(jsonNode.get("Data").spliterator(), false)
        .filter(node -> node.size() > 0).map(node -> new StandardOrder(node))
        .filter(order -> order.getTimestamp().compareTo(startPoint2) > 0)
        .collect(Collectors.toSet());
  }

  void insertOrders(OrderDAO orderDao, Set<Order> orders, Position index) {
    Stopwatch sw = Stopwatch.createStarted();
    orderDao.insert(orders);
    LOGGER.info("{}. insert of {} records took: {}", index, orders.size(), sw.stop());
    orders.clear();
  }

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      Market market = (Market) context.getJobDetail().getJobDataMap().get(MARKET);
      Position index = (Position) context.getJobDetail().getJobDataMap().get(POSITION);
      OrderDAO orderDao =
          (OrderDAO) context.getJobDetail().getJobDataMap().get(RetrieveMarketsJob.ORDER_DAO);
      int tpId = market.getTradePair().getId();
      
      Order lastOrder = orderDao.getLastOrderFor(tpId);
      LocalDateTime lastTime = lastOrder!=null?lastOrder.getTimestamp():null;
      LOGGER.info("{} retrieving orders for: {}", index, market.getLabel());
      Stopwatch marketTime = Stopwatch.createStarted();
      Set<Order> orders = createMarketHistory(tpId, lastTime);
      LOGGER.info("retrieve of {} with {} new records took: {} ms", index, orders.size(),
          marketTime.stop());
      LOGGER.info(orders.size() + " orders to store");
      insertOrders(orderDao, orders, index);
    } catch (Exception e) {
      LOGGER.error("could not store orders", e);
    }

  }
}
