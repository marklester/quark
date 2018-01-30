package quark.trader;

import quark.MarketHistory;
import quark.MarketManager;
import quark.TradePairManager;
import quark.balance.CryptopiaBalanceManager;

public interface Trader {
  CryptopiaBalanceManager getBalanceManager() throws Exception;
  
  MarketManager getMarketManager() throws Exception; 
  
  void order(int tpId, double d);

  TradePairManager getTradePairManager() throws Exception;
  
  public MarketHistory getMarketHistory();
}
