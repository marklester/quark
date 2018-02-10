package quark;

import org.junit.Test;

import quark.model.CurrencyLookup;

public class TradePairsTest {
  @Test
  public void testTradePairManager() throws Exception {
    CurrencyLookup lookup = CurrencyLookup.create();
    CurrencyManager currencyManager = new CurrencyManager(lookup);
    TradePairManager tpManager = TradePairManager.create(currencyManager);
    System.out.println(tpManager.getTradePairs());
  }
}
