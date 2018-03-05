package quark;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import quark.algorithms.so.StochasticOscillatorAlgo;
import quark.balance.MapBalanceManager;
import quark.db.OrderDAO;
import quark.db.cqegine.CollectionOrderDao;
import quark.model.Balance;
import quark.model.CurrencyLookup;
import quark.model.Market;
import quark.model.MonetaryAmount;
import quark.model.StandardMoney;
import quark.model.TradePair;
import quark.orders.Order;
import quark.orders.Order.OrderType;
import quark.orders.OrderBuilder;
import quark.orders.ProcessedOrder;
import quark.report.LapReport;
import quark.report.SimulationReport;
import quark.trader.MockTrader;
import quark.trader.Trader;

public class StochasticOscillatorTest {
  private static final String MARKET_LABEL = "TZC/BTC";

  @Test
  public void testBuyCondition() throws Exception {
    CurrencyLookup usdLookup = createCurrencyLookup();
    CurrencyManager currencyManager = new CurrencyManager(usdLookup);
    MapBalanceManager bm = new MapBalanceManager(currencyManager, 10);

    CryptopiaCurrency tzc = currencyManager.getCurrency("TZC").get();
    CryptopiaCurrency btc = currencyManager.getCurrency("BTC").get();
    TradePair tp = new TradePair(1, MARKET_LABEL, tzc.getSymbol(), tzc, btc.getSymbol(), btc,
        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "Open");
    Market market = new Market(tp, MARKET_LABEL, 1000);
    bm.putBalance(new Balance(btc, BigDecimal.ONE));


    OrderDAO orderDao = new CollectionOrderDao();
    LocalDateTime now = LocalDateTime.now();
    Duration longDuration = Duration.ofDays(3);
    LocalDateTime threeDaysAgo = now.minus(longDuration);
    BigDecimal lastPrice = new BigDecimal(2);
    BigDecimal pastPrice = new BigDecimal(1);
    OrderBuilder template = OrderBuilder.of(tp).hash("hash0").orderType(OrderType.BUY)
        .setAll(lastPrice).orderDate(now);
    
    Order order = template.build();

    Order order2 = template.hash("hash1").price(pastPrice).orderDate(threeDaysAgo).build();
    Order order3 = template.hash("hash2").build();

    ArrayList<Order> orders = Lists.newArrayList(order, order2, order3);
    orderDao.insert(orders);

    Trader trader = new MockTrader(orderDao, bm, Mockito.mock(MarketManager.class));
    StochasticOscillatorAlgo algo = new StochasticOscillatorAlgo(Duration.ofDays(1), 14);
    SimulationReport report = Mockito.mock(SimulationReport.class);
    algo.init(report,LapReport.of(algo, now), trader);
    algo.apply(market, trader);
    Set<ProcessedOrder> porders = algo.executeOrders(trader);
    Assert.assertEquals(1, porders.size());
    ProcessedOrder porder = Iterables.getFirst(porders, null);
    Assert.assertEquals(true, porder.isSuccess());
    Assert.assertEquals(OrderType.BUY, porder.getOrder().getOrderType());
    Assert.assertEquals(new BigDecimal(.1), porder.getOrder().getPercentage());
    
    BigDecimal portion =CoinMath.multiply(BigDecimal.ONE,new BigDecimal(.1));
    //.1
    BigDecimal expectedBtc = BigDecimal.ONE.subtract(portion);
   
    BigDecimal expectedtzc = CoinMath.divide(portion,lastPrice);
    Assert.assertEquals(expectedBtc, bm.getBalance(btc).getAvailable());
    Assert.assertEquals(expectedtzc, bm.getBalance(tzc).getAvailable());
    System.out.println(bm.summary());
  }
  
  CurrencyLookup createCurrencyLookup() {
    StandardMoney btc = new StandardMoney(new BigDecimal(10000), "BTC");
    StandardMoney tzc = new StandardMoney(new BigDecimal(.1), "TZC");
    ImmutableMap<String, MonetaryAmount> usdConversion =
        new ImmutableMap.Builder<String, MonetaryAmount>()
            .put(btc.getSymbol(), btc)
            .put(tzc.getSymbol(),tzc)
            .build();
    return new CurrencyLookup(usdConversion);
  }
  
  @Test
  public void testSellCondition() throws Exception {
    CurrencyLookup usdLookup = createCurrencyLookup();
    CurrencyManager currencyManager = new CurrencyManager(usdLookup);
    MapBalanceManager bm = new MapBalanceManager(currencyManager, 10);

    CryptopiaCurrency tzc = currencyManager.getCurrency("TZC").get();
    CryptopiaCurrency btc = currencyManager.getCurrency("BTC").get();
    TradePair tp = new TradePair(1, MARKET_LABEL, tzc.getSymbol(), tzc, btc.getSymbol(), btc,
        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "Open");
    Market market = new Market(tp, MARKET_LABEL, 1000);
    bm.putBalance(new Balance(tzc, BigDecimal.ONE));


    OrderDAO orderDao = new CollectionOrderDao();
    LocalDateTime now = LocalDateTime.now();
    Duration longDuration = Duration.ofDays(3);
    LocalDateTime threeDaysAgo = now.minus(longDuration);
    BigDecimal lastPrice = new BigDecimal(1);
    BigDecimal pastPrice = new BigDecimal(2);
    OrderBuilder template = OrderBuilder.of(tp).hash("hash0").orderType(OrderType.BUY)
        .setAll(lastPrice).orderDate(now);
    
    Order order = template.build();

    Order order2 = template.hash("hash1").price(pastPrice).orderDate(threeDaysAgo).build();
    Order order3 = template.hash("hash2").build();

    ArrayList<Order> orders = Lists.newArrayList(order, order2, order3);
    orderDao.insert(orders);

    Trader trader = new MockTrader(orderDao, bm, Mockito.mock(MarketManager.class));
    StochasticOscillatorAlgo algo = new StochasticOscillatorAlgo(Duration.ofDays(1), 14);
    SimulationReport report = Mockito.mock(SimulationReport.class);
    algo.init(report,LapReport.of(algo, now), trader);
    algo.apply(market, trader);
    Set<ProcessedOrder> porders = algo.executeOrders(trader);
    Assert.assertEquals(1, porders.size());
    ProcessedOrder porder = Iterables.getFirst(porders, null);
    Assert.assertEquals(true, porder.isSuccess());
    Assert.assertEquals(OrderType.SELL, porder.getOrder().getOrderType());
    Assert.assertEquals(BigDecimal.ONE, porder.getOrder().getPercentage());
    
    BigDecimal expectedBtc = BigDecimal.ONE;
   
    BigDecimal expectedtzc = BigDecimal.ZERO;
    Assert.assertEquals(expectedBtc, bm.getBalance(btc).getAvailable());
    Assert.assertEquals(expectedtzc, bm.getBalance(tzc).getAvailable());
    System.out.println(bm.summary());
  }
}
