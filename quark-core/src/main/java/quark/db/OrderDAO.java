package quark.db;

import java.util.Collection;

import quark.orders.Order;

public interface OrderDAO extends ReadOnlyOrderDAO{
  void insert(Order order);
  
  void insert(Collection<Order> orders);
}
