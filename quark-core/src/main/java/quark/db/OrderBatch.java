package quark.db;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

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
  private static final Logger LOGGER = LoggerFactory.getLogger(OrderIterator.class);
  private LocalDateTime bookMark = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
  private OrderDAO orderDao;
  private Duration batchSize;
  private Set<Order> orders;
  private LocalDateTime bookEnd;

  public OrderIterator(OrderDAO orderDao, Duration batchSize) {
    this.orderDao = orderDao;
    this.batchSize = batchSize;
    bookMark = orderDao.getFirstOrderDate();
    bookEnd = orderDao.getLastOrderDate();
  }

  @Override
  public boolean hasNext() {
    Stopwatch sw = Stopwatch.createStarted();
    LocalDateTime nextBookMark = bookMark.plus(batchSize);
    orders = orderDao.getOrdersFrom(bookMark, nextBookMark);
    bookMark = nextBookMark;
    LOGGER.info("next batch took {}", sw);
    if (nextBookMark.compareTo(bookEnd) > 0) {
      return false;
    }
    return true;
  }

  @Override
  public Set<Order> next() {
    return orders;
  }
}
