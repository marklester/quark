package quark;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;

import quark.model.Market;
import quark.orders.Order;
import quark.orders.OrderDAO;
import quark.orders.StandardOrder;

public class MarketHistory {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private DatabaseManager dbManager;
  private MarketManager mktManager;

  public MarketHistory(DatabaseManager dbManager, MarketManager mktManager) {
    this.dbManager = dbManager;
    this.mktManager = mktManager;
  }
  
  //TODO needs testing specifically for market histroy for dates
  public Set<Order> createMarketHistory(long tradePairId, LocalDateTime startPoint)
      throws Exception {
    LocalDateTime startPoint2 = MoreObjects.firstNonNull(startPoint, LocalDateTime.MIN);
    String getHistoryUrl =
        Trader.BASE_CRYPTOPIA_API_URL + "GetMarketHistory/" + tradePairId + "/48";
    HttpClient client = HttpClientBuilder.create().build();
    HttpGet get = new HttpGet(getHistoryUrl);
    HttpResponse response = client.execute(get);
    JsonNode jsonNode = MAPPER.readTree(response.getEntity().getContent());
    return StreamSupport.stream(jsonNode.get("Data").spliterator(), false)
        .filter(node -> node.size() > 0).map(node -> new StandardOrder(node))
        .filter(order -> order.getTimestamp().compareTo(startPoint2) > 0)
        .collect(Collectors.toSet());
  }
  
  void startPolling() {
    scheduler.scheduleAtFixedRate(() -> {
      storeOrders();
    }, 0, 15, TimeUnit.MINUTES);
  }
  
  private void storeOrders() {
    LocalDateTime lastOrder = getLastOrderDate();
    OrderDAO orderDao = dbManager.getOrderDao();
    Stopwatch total = Stopwatch.createStarted();
    Collection<Market> markets = mktManager.getMarkets();
    LongAdder count = new LongAdder();
    markets.stream().parallel().forEach(market -> {
      System.out.printf("(%s/%s) retrieving orders for: %s\n", count, markets.size(),
          market.getLabel());
      Stopwatch marketTime = Stopwatch.createStarted();
      try {
        Set<Order> orders = createMarketHistory(market.getTradePair().getId(), lastOrder);
        System.out.printf("retrieve history took: %s ms\n",
            marketTime.stop().elapsed(TimeUnit.MILLISECONDS));
        System.out.println(orders.size() + " orders to store");
        Stopwatch watch = Stopwatch.createStarted();
        orderDao.insert(orders);
        System.out.printf("insert took: %s ms\n", watch.stop().elapsed(TimeUnit.MILLISECONDS));
        count.increment();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    System.out.printf("took %s m", total.stop().elapsed(TimeUnit.MINUTES));
  }

  private LocalDateTime getLastOrderDate() {
    return dbManager.getOrderDao().getLastOrder();
  }
}
