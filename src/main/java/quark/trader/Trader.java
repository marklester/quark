package quark.trader;

import java.math.BigDecimal;

import quark.MarketManager;
import quark.balance.BalanceManager;
import quark.db.OrderDAO;
import quark.model.TradePair;
import quark.orders.Order.OrderType;

public interface Trader {
  BalanceManager getBalanceManager() throws Exception;
  
  MarketManager getMarketManager() throws Exception; 
  
  void buy(TradePair tpId, double d);
  
  void sell(TradePair tpId, double d);

  void order(TradePair tradePair, OrderType type, BigDecimal price, BigDecimal amount);

  OrderDAO getOrderDao();
}
