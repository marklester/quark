package quark;

import static org.junit.Assert.fail;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import quark.balance.BalanceManager;
import quark.balance.MapBalanceManager;
import quark.db.OrderDAO;
import quark.model.Balance;
import quark.model.CurrencyLookup;
import quark.model.TradePair;
import quark.orders.Order.OrderType;
import quark.orders.StandardOrder;
import quark.trader.MockTrader;

public class MockTraderTest {
  CurrencyManager currencyManager;

  @Before
  public void setup() throws Exception {
    CurrencyLookup lookup =
        CurrencyLookup.from(new File("src/test/resources/coinmarketcap_currencies.json"));
    currencyManager = new CurrencyManager(lookup);
  }

  @Test
  public void testSell() throws Exception {
    int tpID = 5659;
    OrderDAO orderDAO = Mockito.mock(OrderDAO.class);
    BigDecimal price = new BigDecimal(.0001, new MathContext(8, RoundingMode.HALF_EVEN));
    BigDecimal total = new BigDecimal(1);
    BigDecimal amount = new BigDecimal(1);
    StandardOrder order =
        new StandardOrder("", tpID, OrderType.BUY, "", price, total, amount, LocalDateTime.now());
    Mockito.when(orderDAO.getLastOrderFor(Mockito.anyInt())).thenReturn(order);
    TradePairManager tpManager = TradePairManager.create(currencyManager);
    MarketManager marketManager = new MarketManager(tpManager);

    BalanceManager balanceManager = new MapBalanceManager(currencyManager, 10);
    balanceManager
        .putBalance(new Balance(currencyManager.getCurrency("TZC").get(), new BigDecimal(10000)));

    MockTrader trader = new MockTrader(orderDAO, balanceManager, marketManager);

    TradePair tp = tpManager.getTradePair(tpID);
    //trader.execute(tp, 1.0);
    BigDecimal actualCoin = balanceManager.getBalance(tp.getCurrency().getId()).getAvailable();
    Assert.assertThat(actualCoin, Matchers.comparesEqualTo(new BigDecimal(0)));
    BigDecimal baseCoin = balanceManager.getBalance(tp.getBaseCurrency().getId()).getAvailable();
    Assert.assertThat(baseCoin, Matchers.comparesEqualTo(new BigDecimal(1)));
  }

  @Test
  public void testFailedSell() throws Exception {
    int tpID = 5659;
    OrderDAO orderDAO = Mockito.mock(OrderDAO.class);
    BigDecimal price = new BigDecimal(.0001, new MathContext(8, RoundingMode.HALF_EVEN));
    BigDecimal total = new BigDecimal(1);
    BigDecimal amount = new BigDecimal(1);
    StandardOrder order =
        new StandardOrder("", tpID, OrderType.BUY, "", price, total, amount, LocalDateTime.now());
    Mockito.when(orderDAO.getLastOrderFor(Mockito.anyInt())).thenReturn(order);
    TradePairManager tpManager = TradePairManager.create(currencyManager);
    MarketManager marketManager = new MarketManager(tpManager);

    BalanceManager balanceManager = new MapBalanceManager(currencyManager, 1);
    BigDecimal expectedAmount = new BigDecimal(10000);
    balanceManager
        .putBalance(new Balance(currencyManager.getCurrency("TZC").get(), expectedAmount));

    MockTrader trader = new MockTrader(orderDAO, balanceManager, marketManager);

    TradePair tp = tpManager.getTradePair(tpID);
    try {
      //trader.sell(tp, 1.0);
      fail("sell should have failed");
    } catch (IllegalArgumentException iae) {

    }
    BigDecimal actualCoin = balanceManager.getBalance(tp.getCurrency().getId()).getAvailable();
    Assert.assertThat(actualCoin, Matchers.comparesEqualTo(new BigDecimal(10000)));
    BigDecimal baseCoin = balanceManager.getBalance(tp.getBaseCurrency().getId()).getAvailable();
    Assert.assertThat(baseCoin, Matchers.comparesEqualTo(BigDecimal.ZERO));
  }

  @Test
  public void testBuy() throws Exception {
    int tpID = 5659;
    OrderDAO orderDAO = Mockito.mock(OrderDAO.class);

    BigDecimal price = new BigDecimal(.0001, new MathContext(8, RoundingMode.HALF_EVEN));
    BigDecimal total = new BigDecimal(1);
    BigDecimal amount = new BigDecimal(1);
    StandardOrder order =
        new StandardOrder("", tpID, OrderType.BUY, "", price, total, amount, LocalDateTime.now());
    Mockito.when(orderDAO.getLastOrderFor(Mockito.anyInt())).thenReturn(order);

    TradePairManager tpManager = TradePairManager.create(currencyManager);
    MarketManager marketManager = new MarketManager(tpManager);

    BalanceManager balanceManager = new MapBalanceManager(currencyManager, 10);
    balanceManager
        .putBalance(new Balance(currencyManager.getCurrency("BTC").get(), new BigDecimal(1)));

    MockTrader trader = new MockTrader(orderDAO, balanceManager, marketManager);

    TradePair tp = tpManager.getTradePair(tpID);
    //trader.buy(tp, 1.0);
    BigDecimal actualCoin = balanceManager.getBalance(tp.getCurrency().getId()).getAvailable();
    Assert.assertThat(actualCoin, Matchers.comparesEqualTo(new BigDecimal(10000)));
    BigDecimal baseCoin = balanceManager.getBalance(tp.getBaseCurrency().getId()).getAvailable();
    Assert.assertThat(baseCoin, Matchers.comparesEqualTo(BigDecimal.ZERO));
  }


  @Test
  public void testFailedBuy() throws Exception {
    int tpID = 5659;
    OrderDAO orderDAO = Mockito.mock(OrderDAO.class);

    BigDecimal price = new BigDecimal(.0001, new MathContext(8, RoundingMode.HALF_EVEN));
    BigDecimal total = new BigDecimal(1);
    BigDecimal amount = new BigDecimal(1);
    StandardOrder order =
        new StandardOrder("", tpID, OrderType.BUY, "", price, total, amount, LocalDateTime.now());
    Mockito.when(orderDAO.getLastOrderFor(Mockito.anyInt())).thenReturn(order);

    TradePairManager tpManager = TradePairManager.create(currencyManager);
    MarketManager marketManager = new MarketManager(tpManager);

    BalanceManager balanceManager = new MapBalanceManager(currencyManager, 1);
    balanceManager
        .putBalance(new Balance(currencyManager.getCurrency("BTC").get(), new BigDecimal(1)));

    MockTrader trader = new MockTrader(orderDAO, balanceManager, marketManager);

    TradePair tp = tpManager.getTradePair(tpID);
    try {
      //trader.buy(tp, 1.0);
      fail("buy should have failed");
    } catch (IllegalArgumentException iae) {

    }
    BigDecimal baseCoin = balanceManager.getBalance(tp.getBaseCurrency().getId()).getAvailable();
    Assert.assertThat(baseCoin, Matchers.comparesEqualTo(BigDecimal.ONE));
  }
}
