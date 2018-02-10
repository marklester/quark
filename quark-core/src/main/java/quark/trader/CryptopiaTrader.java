package quark.trader;

import java.math.BigDecimal;
import java.util.Set;

import quark.CurrencyManager;
import quark.MarketManager;
import quark.TradePairManager;
import quark.balance.BalanceListing;
import quark.balance.BalanceManager;
import quark.balance.CryptopiaBalanceManager;
import quark.db.DatabaseManager;
import quark.db.OrderDAO;
import quark.model.OpenOrder;
import quark.model.TradePair;
import quark.orders.AlgoOrder;
import quark.orders.ProcessedOrder;
import quark.orders.Order.OrderType;
import quark.populator.MarketHistory;

public class CryptopiaTrader implements Trader {
  private MarketHistory marketHistory;
  private MarketManager marketManager;
  private CurrencyManager currencyManager;
  private BalanceListing balanceManager;
  private DatabaseManager dbManager;

  public CryptopiaTrader(DatabaseManager dbManager, CurrencyManager currencyManager,
      MarketManager marketManager) throws Exception {
    this.marketManager = marketManager;
    this.currencyManager = currencyManager;
    this.dbManager = dbManager;
    balanceManager = new CryptopiaBalanceManager(currencyManager);
  }

  public BalanceManager getBalanceManager() throws Exception {
    return null;
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

  @Override
  public void buy(TradePair tpId, double d) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void sell(TradePair tpId, double d) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void order(TradePair tradePair, OrderType type, BigDecimal price, BigDecimal amount) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public OrderDAO getOrderDao() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ProcessedOrder execute(AlgoOrder order) {
    // TODO Auto-generated method stub
    return null;
  }
}
