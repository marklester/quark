package quark;

import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import quark.algorithms.MovingAverageAlgo;
import quark.balance.MapBalanceManager;
import quark.db.OrderDAO;
import quark.model.Market;
import quark.trader.Trader;

public class MovingAvgAlgorithmTest {
  @Test
  public void testBuyCondition() throws Exception {
    CurrencyManager currencyManager = new CurrencyManager();
    MapBalanceManager bm = new MapBalanceManager(currencyManager);

    Market market = Mockito.mock(Market.class);
    Trader trader = Mockito.mock(Trader.class);

    OrderDAO orderDao = Mockito.mock(OrderDAO.class);

    Mockito.when(trader.getBalanceManager()).thenReturn(bm);
    Mockito.when(trader.getOrderDao()).thenReturn(orderDao);

    MovingAverageAlgo algo = new MovingAverageAlgo();
    algo.apply(market, trader);
    BigDecimal expectedBalance = new BigDecimal(0);
    Assert.assertTrue(expectedBalance.compareTo(bm.getBalance(0).getAvailable()) == 0);
  }

  @Test
  public void testSellCondition() {
    fail("test sell not done yet");
  }
}
