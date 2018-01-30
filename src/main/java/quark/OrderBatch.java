package quark;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;

import quark.db.OrderDAO;
import quark.orders.Order;

public class OrderBatch implements Iterable<Set<Order>> {
  private OrderDAO orderDao;
  private Duration timePartition;

  public OrderBatch(OrderDAO orderDao, Duration timeParititon) {
    this.orderDao = orderDao;
    this.timePartition = timeParititon;
  }

  @Override
  public Iterator<Set<Order>> iterator() {
    return new OrderIterator(orderDao, timePartition);
  }

}


class OrderIterator implements Iterator<Set<Order>> {
  LocalDateTime bookMark = LocalDateTime.MIN;
  private OrderDAO orderDao;
  private Duration batchSize;
  private Set<Order> orders;

  public OrderIterator(OrderDAO orderDao, Duration batchSize) {
    this.orderDao = orderDao;
    this.batchSize = batchSize;
  }

  @Override
  public boolean hasNext() {
    LocalDateTime nextBookMark = bookMark.plus(batchSize);
    orders = orderDao.getOrdersFrom(bookMark, nextBookMark);
    if (orders.isEmpty()) {
      orders = null;
      return false;
    }
    return true;
  }

  @Override
  public Set<Order> next() {
    return orders;
  }
}
