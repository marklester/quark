package quark.db;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;

import quark.model.IPriceRange;
import quark.orders.Order;
import quark.orders.Order.OrderType;

public interface ReadOnlyOrderDAO {
    
  BigDecimal getAvg(int tradePairId, Duration overTime);
  
  LocalDateTime getLastOrderDate();
  
  Set<Order> getOrdersFrom(LocalDateTime start, LocalDateTime end);
  
  Map<Integer, Order> getLastOrders();  

  Order getLastOrderFor(int tpId);
  
  Set<Order> getOrders(int tpId);
  
  int getOrderCount(int tpId, OrderType type);
  
  Stream<Order> getOrders();

  LocalDateTime getFirstOrderDate();

  DSLContext getContext();

  Table<Record> getTable();
  
  void setTable(Table<Record> table);

  Map<Integer, BigDecimal> getAllAvg(LocalDateTime start, Duration overTime);

  Map<String, Integer> countOrdersBy(String dateTimePattern);

  Integer getOrderCount();
  
  Map<Integer, ? extends IPriceRange> getPriceRanges(LocalDateTime anchor, Duration overTime);


}
