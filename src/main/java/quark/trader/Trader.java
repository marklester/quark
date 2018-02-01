package quark.trader;

import quark.MarketManager;
import quark.TradePairManager;
import quark.balance.CryptopiaBalanceManager;
import quark.db.DatabaseManager;

public interface Trader {
  CryptopiaBalanceManager getBalanceManager() throws Exception;
  
  MarketManager getMarketManager() throws Exception; 
  
  void order(int tpId, double d);

  TradePairManager getTradePairManager() throws Exception;
  
  public DatabaseManager getDBManager();
}
