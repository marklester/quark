package quark.db;

import quark.MarketSimulator;

public interface DatabaseManager {
  public OrderDAO getOrderDao();

  public MarketSimulator getMarketSimulator();
}
