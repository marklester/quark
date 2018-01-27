package quark.trader;

import java.util.Set;

import quark.CurrencyManager;
import quark.DatabaseManager;
import quark.MarketHistory;
import quark.MarketManager;
import quark.TradePairManager;
import quark.balance.BalanceManager;
import quark.market.stats.MarketStats;
import quark.model.OpenOrder;

public class CryptopiaTrader implements Trader{
  private MarketHistory marketHistory;
  private MarketManager marketManager;
  private TradePairManager tradePairManager;
  private CurrencyManager currencyManager;
  private BalanceManager balanceManager;
  
  public CryptopiaTrader() throws Exception {
    DatabaseManager dbManager = new DatabaseManager();
    dbManager.start();
    currencyManager = new CurrencyManager();
    balanceManager = new BalanceManager(getCurrencyManager());
    tradePairManager = TradePairManager.create();
    marketManager = MarketManager.create(getTradePairManager());
    marketHistory = new MarketHistory(dbManager, getMarketManager());
  }
  public BalanceManager getBalanceManager() throws Exception {
    return balanceManager;
  }

  private CurrencyManager getCurrencyManager() {
    return currencyManager;
  }

  public MarketManager getMarketManager() throws Exception {
    return marketManager;
  }
  
  public TradePairManager getTradePairManager() throws Exception {
    return tradePairManager;
  }
  
  public MarketHistory getMarketHistory() {
    return marketHistory;
  }
  
  public Set<OpenOrder> getOpenOrders(){
    return null;
  }
  
  public Set<OpenOrder> getOpenOrders(long tpId){
    return null;
  }
  
  /**
   * 
   * @param tpId the coin to buy
   * @param the percent of balance to use
   */
  public void order(long tpId, double d) {
    // TODO Auto-generated method stub
    
  }

  public MarketStats getMarketStats() {
    // TODO Auto-generated method stub
    return null;
  }
}
