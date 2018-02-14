package quark.populator;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
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

class GetMarketHistory implements Callable<Boolean> {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final Logger LOGGER = LoggerFactory.getLogger(GetMarketHistory.class);
  private final Market market;
  private final Position index;
  private LocalDateTime lastOrder;

  private OrderDAO orderDao;

  public GetMarketHistory(OrderDAO orderDao, Market market, Position index,
      LocalDateTime lastOrder) {
    this.market = market;
    this.index = index;
    this.lastOrder = lastOrder;
    this.orderDao = orderDao;
  }

  // TODO needs testing specifically for market history for dates
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

  @Override
  public Boolean call() throws Exception {
    try {
    LOGGER.info("{} retrieving orders for: {}", index, market.getLabel());
    Stopwatch marketTime = Stopwatch.createStarted();
    Set<Order> orders = createMarketHistory(market.getTradePair().getId(), lastOrder);
    LOGGER.info("retrieve of {} with {} new records took: {} ms", index, orders.size(),
        marketTime.stop().elapsed(TimeUnit.MILLISECONDS));
    LOGGER.info(orders.size() + " orders to store");
    insertOrders(orders);
    }catch(Exception e) {
      LOGGER.error("could not store orders",e);
    }
    return true;
  }

  void insertOrders(Set<Order> orders) {
    Stopwatch sw = Stopwatch.createStarted();
    orderDao.insert(orders);
    LOGGER.info("{}. insert of {} records took: {}", index, orders.size(),
        sw.elapsed(TimeUnit.MILLISECONDS));
    orders.clear();
  }
}
