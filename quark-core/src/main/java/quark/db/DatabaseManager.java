package quark.db;

import java.time.Duration;

import quark.simulation.MarketSimulator;

public interface DatabaseManager {
  public OrderDAO getOrderDao();

  public MarketSimulator getMarketSimulator(Duration tickRate);
}
