package quark.simulation;

import java.time.LocalDateTime;

import quark.db.ReadOnlyOrderDAO;

public interface MarketSimulator extends Iterable<LocalDateTime> {
  public ReadOnlyOrderDAO getOrderDao();
}
