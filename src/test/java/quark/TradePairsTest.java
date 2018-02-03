package quark;

import org.junit.Test;

public class TradePairsTest {
  @Test
  public void testTradePairManager() throws Exception {
    CurrencyManager currencyManager = new CurrencyManager();
    TradePairManager tpManager = TradePairManager.create(currencyManager);
    System.out.println(tpManager.getTradePairs());
  }
}
