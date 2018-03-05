package quark.simulation;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;

import quark.db.OrderBatcher;
import quark.db.OrderDAO;
import quark.orders.Order;

class BatchInsertIterator implements Iterator<LocalDateTime> {
  private Iterator<Set<Order>> orderIterator;
  private OrderDAO destDao;

  public BatchInsertIterator(OrderBatcher orderBatch, OrderDAO destDao) {
    this.orderIterator = orderBatch.iterator();
    this.destDao = destDao;
  }

  @Override
  public boolean hasNext() {
    return orderIterator.hasNext();
  }

  @Override
  public LocalDateTime next() {
    destDao.insert(orderIterator.next());
    return destDao.getLastOrderDate();
  }

}