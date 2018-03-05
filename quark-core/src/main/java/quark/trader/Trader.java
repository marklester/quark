package quark.trader;

import java.math.BigDecimal;

import quark.MarketManager;
import quark.balance.BalanceManager;
import quark.db.ReadOnlyOrderDAO;
import quark.model.TradePair;
import quark.orders.AlgoOrder;
import quark.orders.ProcessedOrder;
import quark.orders.Order.OrderType;

public interface Trader {
  BalanceManager getBalanceManager() throws Exception;
  
  MarketManager getMarketManager() throws Exception; 

  void order(TradePair tradePair, OrderType type, BigDecimal price, BigDecimal amount);

  ReadOnlyOrderDAO getOrderDao();

  ProcessedOrder execute(AlgoOrder order);
}
