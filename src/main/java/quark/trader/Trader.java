package quark.trader;

import quark.MarketHistory;
import quark.MarketManager;
import quark.TradePairManager;
import quark.balance.BalanceManager;
import quark.market.stats.MarketStats;

public interface Trader {
  BalanceManager getBalanceManager() throws Exception;
  
  MarketManager getMarketManager() throws Exception; 
  
  MarketStats getMarketStats();
  
  void order(long tpId, double d);

  TradePairManager getTradePairManager() throws Exception;
  
  public MarketHistory getMarketHistory();
}
