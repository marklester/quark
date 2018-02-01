package quark.trader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quark.MarketManager;
import quark.TradePairManager;
import quark.balance.BalanceManager;
import quark.balance.CryptopiaBalanceManager;
import quark.balance.MapBalanceManager;
import quark.db.DatabaseManager;

public class TestTrader implements Trader {
  private static Logger LOGGER = LoggerFactory.getLogger(TestTrader.class);

  private BalanceManager balanceManager = new MapBalanceManager();

  private DatabaseManager dbManager;

  public TestTrader(DatabaseManager dbManager) {
    this.dbManager = dbManager;
  }

  @Override
  public MarketManager getMarketManager() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void order(int tpId, double d) {
    dbManager.getOrderDao().getLastOrderFor(tpId);
    LOGGER.info("trading for {} with {}% of balance");
  }

  @Override
  public TradePairManager getTradePairManager() {
    return null;
  }

  @Override
  public CryptopiaBalanceManager getBalanceManager() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DatabaseManager getDBManager() {
    return dbManager;
  }

}
