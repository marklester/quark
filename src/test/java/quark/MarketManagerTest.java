package quark;

import org.junit.Assert;
import org.junit.Test;

public class MarketManagerTest {
  @Test
  public void testMarketManager() throws Exception{
    TradePairManager tradePairManager = TradePairManager.create();
    MarketManager manager = new MarketManager(tradePairManager);
    Assert.assertTrue(manager.getMarkets().size()>0);
  }
}
