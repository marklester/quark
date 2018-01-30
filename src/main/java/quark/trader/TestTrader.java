package quark.trader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quark.MarketHistory;
import quark.MarketManager;
import quark.TradePairManager;
import quark.balance.BalanceManager;
import quark.balance.CryptopiaBalanceManager;
import quark.balance.MapBalanceManager;

public class TestTrader implements Trader{
  private static Logger LOGGER = LoggerFactory.getLogger(TestTrader.class);
  
  private MarketHistory marketHistory;
  private BalanceManager balanceManager = new MapBalanceManager();
  
  public TestTrader(MarketHistory marketHistory) {
    this.marketHistory = marketHistory;
  }

  @Override
  public MarketManager getMarketManager() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void order(int tpId, double d) {
    marketHistory.getOrderDAO().getLastOrderFor(tpId);
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
  public MarketHistory getMarketHistory() {
    return marketHistory;
  }

}
