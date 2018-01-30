package quark.db;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

import quark.orders.Order;

public interface OrderDAO {
  
  void insert(Order order);
  
  void insert(Collection<Order> orders);
  
  BigDecimal getAvg(int tradePairId, Duration overTime);
  
  LocalDateTime getLastOrderDate();
  
  Set<Order> getOrdersFrom(LocalDateTime start, LocalDateTime end);

  Order getLastOrderFor(int tpId);
  
  Set<Order> getOrders(int tpId);
}
