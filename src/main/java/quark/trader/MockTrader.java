package quark.trader;

import quark.MarketHistory;
import quark.MarketManager;
import quark.TradePairManager;
import quark.balance.BalanceManager;
import quark.market.stats.MarketStats;

public class MockTrader implements Trader{
  @Override
  public MarketManager getMarketManager() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MarketStats getMarketStats() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void order(long tpId, double d) {

    
  }

  @Override
  public TradePairManager getTradePairManager() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BalanceManager getBalanceManager() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MarketHistory getMarketHistory() {
    // TODO Auto-generated method stub
    return null;
  }

}
