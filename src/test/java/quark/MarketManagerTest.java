package quark;

import org.junit.Assert;
import org.junit.Test;

public class MarketManagerTest {
  @Test
  public void testMarketManager() throws Exception{
    CurrencyManager currencyManager = new CurrencyManager();
    TradePairManager tradePairManager = TradePairManager.create(currencyManager);
    MarketManager manager = new MarketManager(tradePairManager);
    Assert.assertTrue(manager.getMarkets().size()>0);
  }
}
