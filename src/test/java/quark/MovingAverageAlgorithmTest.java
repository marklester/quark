package quark;

import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableMap;

import quark.algorithms.MovingAverageAlgo;
import quark.balance.MapBalanceManager;
import quark.db.OrderDAO;
import quark.model.Balance;
import quark.model.Market;
import quark.model.TradePair;
import quark.trader.Trader;

public class MovingAverageAlgorithmTest {
  @Test
  public void testBuyCondition() throws Exception {
    CurrencyManager currencyManager = new CurrencyManager();
    MapBalanceManager bm = new MapBalanceManager(currencyManager, 10);

    CryptopiaCurrency tzc = currencyManager.getCurrency("TZC").get();
    CryptopiaCurrency btc = currencyManager.getCurrency("BTC").get();
    TradePair tp = new TradePair(1, "TZC/BTC", "TZC", tzc, "BTC", btc, BigDecimal.ZERO,
        BigDecimal.ZERO, BigDecimal.ZERO, "Open");
    Market market = new Market(tp, "TZC/BTC", 1000);
    bm.putBalance(new Balance(btc, BigDecimal.ONE));
    Trader trader = Mockito.mock(Trader.class);

    OrderDAO orderDao = Mockito.mock(OrderDAO.class);
    Map<Integer, BigDecimal> oneDay = ImmutableMap.of(tp.getId(), new BigDecimal(2));
    Map<Integer, BigDecimal> threeDay = ImmutableMap.of(tp.getId(), BigDecimal.ONE);
    Mockito.when(orderDao.getAllAvg(Duration.ofDays(1))).thenReturn(oneDay);
    Mockito.when(orderDao.getAllAvg(Duration.ofDays(3))).thenReturn(threeDay);

    Mockito.when(trader.getBalanceManager()).thenReturn(bm);
    Mockito.when(trader.getOrderDao()).thenReturn(orderDao);

    MovingAverageAlgo algo = new MovingAverageAlgo();
    algo.init(trader);
    algo.apply(market, trader);
    BigDecimal expectedBalance = BigDecimal.ZERO;
    Assert.assertTrue(expectedBalance.compareTo(bm.getBalance(tzc.getId()).getAvailable()) == 0);
  }

  @Test
  public void testSellCondition() {
    fail("test sell not done yet");
  }
}
