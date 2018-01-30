package quark;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
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

import quark.db.DatabaseManager;
import quark.db.OrderDAO;
import quark.model.Market;
import quark.orders.Order;
import quark.orders.StandardOrder;

public class MarketHistory {
  private static final Logger LOGGER = LoggerFactory.getLogger(MarketHistory.class);
  private DatabaseManager dbManager;
  private MarketManager mktManager;

  public MarketHistory(DatabaseManager dbManager, MarketManager mktManager) {
    this.dbManager = dbManager;
    this.mktManager = mktManager;
  }
  
  public OrderDAO getOrderDAO(){
    return dbManager.getOrderDao();
  }

  public void startPolling() {
    try {
      storeOrders();
    } catch (ExecutionException e) {
      LOGGER.error("could not store orders",e);
    }
    // scheduler.scheduleAtFixedRate(() -> {
    // storeOrders();
    // }, 0, 15, TimeUnit.MINUTES);
    // try {
    // scheduler.awaitTermination(1, TimeUnit.DAYS);
    // } catch (Exception e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
  }

  private ExecutorCompletionService<Boolean> executor =
      new ExecutorCompletionService<>(Executors.newFixedThreadPool(5));

  private void storeOrders() throws ExecutionException {

    LocalDateTime lastOrder = getLastOrderDate();
    OrderDAO orderDao = dbManager.getOrderDao();
    LOGGER.info("Getting orders after: " + lastOrder);
    Stopwatch total = Stopwatch.createStarted();
    Collection<Market> markets = mktManager.getMarkets();
    int count = 0;
    for (Market market : markets) {
      Callable<Boolean> history =
          new GetMarketHistory(orderDao,market, new Position(count, markets.size()), lastOrder);
      executor.submit(history);
      count += 1;
    } 
    
    markets.stream().forEach(mkt->{
      try {
        executor.take();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    });
    LOGGER.info("took {} mins", total.stop().elapsed(TimeUnit.MINUTES));
  }

  private LocalDateTime getLastOrderDate() {
    return dbManager.getOrderDao().getLastOrderDate();
  }
}


class Position {
  final int position;
  final int total;

  public Position(int position, int total) {
    this.position = position;
    this.total = total;
  }

  public int getPosition() {
    return position;
  }

  public int getTotal() {
    return total;
  }

  public String toString() {
    return String.format("(%s/%s)", position, total);
  }
}


class GetMarketHistory implements Callable<Boolean> {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final Logger LOGGER = LoggerFactory.getLogger(GetMarketHistory.class);
  private final Market market;
  private final Position index;
  private LocalDateTime lastOrder;

  private OrderDAO orderDao;

  public GetMarketHistory(OrderDAO orderDao,Market market, Position index, LocalDateTime lastOrder) {
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
    LOGGER.info("{} retrieving orders for: {}", index, market.getLabel());
    Stopwatch marketTime = Stopwatch.createStarted();
    Set<Order> orders = createMarketHistory(market.getTradePair().getId(), lastOrder);
    LOGGER.info("retrieve of {} with {} new records took: {} ms", index, orders.size(),
        marketTime.stop().elapsed(TimeUnit.MILLISECONDS));
    LOGGER.info(orders.size() + " orders to store");
    insertOrders(orders);
    return true;
  }
  
  void insertOrders(Set<Order> orders){
    Stopwatch sw = Stopwatch.createStarted();
    orderDao.insert(orders);
    LOGGER.info("{}. insert of {} records took: {}", index, orders.size(),
        sw.elapsed(TimeUnit.MILLISECONDS));
    orders.clear();
  }
}
