package quark.trader;

import java.util.Set;

import quark.CurrencyManager;
import quark.MarketManager;
import quark.TradePairManager;
import quark.balance.BalanceManager;
import quark.balance.CryptopiaBalanceManager;
import quark.db.DatabaseManager;
import quark.model.OpenOrder;
import quark.model.TradePair;
import quark.populator.MarketHistory;

public class CryptopiaTrader implements Trader {
  private MarketHistory marketHistory;
  private MarketManager marketManager;
  private CurrencyManager currencyManager;
  private BalanceManager balanceManager;
  private DatabaseManager dbManager;

  public CryptopiaTrader(DatabaseManager dbManager, CurrencyManager currencyManager,
      MarketManager marketManager) throws Exception {
    this.marketManager = marketManager;
    this.currencyManager = currencyManager;
    this.dbManager = dbManager;
    balanceManager = new CryptopiaBalanceManager(currencyManager);
  }

  public BalanceManager getBalanceManager() throws Exception {
    return balanceManager;
  }

  public CurrencyManager getCurrencyManager() {
    return currencyManager;
  }

  public MarketManager getMarketManager() throws Exception {
    return marketManager;
  }

  public TradePairManager getTradePairManager() throws Exception {
    return marketManager.getTradePairManager();
  }

  public MarketHistory getMarketHistory() {
    return marketHistory;
  }

  public Set<OpenOrder> getOpenOrders() {
    return null;
  }

  public Set<OpenOrder> getOpenOrders(long tpId) {
    return null;
  }

  /**
   * 
   * @param tpId the coin to buy
   * @param the percent of balance to use
   */
  public void order(TradePair tp, double d) {
    // TODO Auto-generated method stub

  }
}
