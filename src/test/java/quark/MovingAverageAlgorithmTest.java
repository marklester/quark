package quark;

import static org.junit.Assert.fail;

import java.io.File;
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

import quark.algorithms.MovingAverageAlgo;
import quark.balance.MapBalanceManager;
import quark.db.CollectionOrderDao;
import quark.db.OrderDAO;
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
import quark.trader.MockTrader;
import quark.trader.Trader;

public class MovingAverageAlgorithmTest {
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

    OrderBuilder template = OrderBuilder.of(tp).hash("hash0").orderType(OrderType.BUY)
        .setAll(new BigDecimal(2)).orderDate(now);
    Order order = template.build();

    Order order2 = template.hash("hash1").price(new BigDecimal(1)).orderDate(threeDaysAgo).build();
    Order order3 = template.hash("hash2").build();

    ArrayList<Order> orders = Lists.newArrayList(order, order2, order3);
    orderDao.insert(orders);

    Trader trader = new MockTrader(orderDao, bm, Mockito.mock(MarketManager.class));
    MovingAverageAlgo algo = new MovingAverageAlgo(Duration.ofDays(1), longDuration);
    algo.init(now, trader);
    algo.apply(market, trader);
    Set<ProcessedOrder> porders = algo.executeOrders(trader);
    Assert.assertEquals(1, porders.size());
    ProcessedOrder porder = Iterables.getFirst(porders, null);
    Assert.assertEquals(true, porder.isSuccess());
    Assert.assertEquals(OrderType.BUY, porder.getOrder().getOrderType());
    Assert.assertEquals(.1, porder.getOrder().getPercentage(), .001);
    // BigDecimal expectedBalance = BigDecimal.ZERO;
    // Assert.assertTrue(expectedBalance.compareTo(bm.getBalance(tzc.getId()).getAvailable()) == 0);
    // Assert.assertTrue(expectedBalance.compareTo(bm.getBalance(tzc.getId()).getAvailable()) == 0);
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
  public void testSellCondition() {
    fail("test sell not done yet");
  }
}
