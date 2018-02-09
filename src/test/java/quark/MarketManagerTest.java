package quark;

import org.junit.Assert;
import org.junit.Test;

import quark.model.CurrencyLookup;

public class MarketManagerTest {
  @Test
  public void testMarketManager() throws Exception{
    CurrencyLookup lookup = CurrencyLookup.create();
    CurrencyManager currencyManager = new CurrencyManager(lookup);
    TradePairManager tradePairManager = TradePairManager.create(currencyManager);
    MarketManager manager = new MarketManager(tradePairManager);
    Assert.assertTrue(manager.getMarkets().size()>0);
  }
}
