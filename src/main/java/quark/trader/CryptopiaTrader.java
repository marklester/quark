package quark.trader;

import java.util.Set;

import quark.CurrencyManager;
import quark.MarketHistory;
import quark.MarketManager;
import quark.TradePairManager;
import quark.balance.CryptopiaBalanceManager;
import quark.db.DatabaseManager;
import quark.model.OpenOrder;

public class CryptopiaTrader implements Trader {
  private MarketHistory marketHistory;
  private MarketManager marketManager;
  private CurrencyManager currencyManager;
  private CryptopiaBalanceManager balanceManager;

  public CryptopiaTrader(DatabaseManager dbManager, CurrencyManager currencyManager,
      MarketHistory marketHistory, MarketManager marketManager) throws Exception {
    this.marketManager = marketManager;
    this.currencyManager = currencyManager;
    this.marketHistory = marketHistory;
    balanceManager = new CryptopiaBalanceManager(currencyManager);
  }

  public CryptopiaBalanceManager getBalanceManager() throws Exception {
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
  public void order(int tpId, double d) {
    // TODO Auto-generated method stub

  }
}
