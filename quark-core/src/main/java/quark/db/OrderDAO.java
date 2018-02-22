package quark.db;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;

import quark.orders.Order;
import quark.orders.Order.OrderType;

public interface OrderDAO {
  
  void insert(Order order);
  
  void insert(Collection<Order> orders);
  
  BigDecimal getAvg(int tradePairId, Duration overTime);
  
  LocalDateTime getLastOrderDate();
  
  Set<Order> getOrdersFrom(LocalDateTime start, LocalDateTime end);

  Order getLastOrderFor(int tpId);
  
  Set<Order> getOrders(int tpId);
  
  int getOrderCount(int tpId, OrderType type);
  
  Stream<Order> getOrders();

  LocalDateTime getFirstOrderDate();

  DSLContext getContext();

  Table<Record> getTable();

  Map<Integer, BigDecimal> getAllAvg(LocalDateTime start, Duration overTime);

  Map<String, Integer> countOrdersBy(String dateTimePattern);

  Integer getOrderCount();
}
