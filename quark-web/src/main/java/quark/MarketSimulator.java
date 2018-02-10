package quark;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import quark.db.OrderBatch;
import quark.db.OrderCopier;
import quark.db.OrderDAO;
import quark.db.OrderFields;
import quark.db.OrderRecordMapper;
import quark.db.PostgresOrderDAO;
import quark.orders.Order;

// TODO use insert into x select x from ...
public class MarketSimulator implements Iterable<LocalDateTime> {
  private Duration tickRate;
  private DSLContext ctx;
  private String tempTableName;
  private OrderDAO sourceDao;
  private PostgresOrderDAO destDao;

  public MarketSimulator(DSLContext ctx, Duration tickRate, OrderDAO sourceDao) {
    this.tickRate = tickRate;
    this.ctx = ctx;
    this.tempTableName = "msorders";
    this.sourceDao = sourceDao;
    prepare();
  }

  public void prepare() {
    String query = String.format("CREATE TEMPORARY TABLE %s (like %s including all)", tempTableName,
        OrderFields.ORDERS.getName());
    ctx.execute(query);
    destDao = new PostgresOrderDAO(ctx, new OrderRecordMapper(), DSL.table(tempTableName));
  }

  @Override
  public Iterator<LocalDateTime> iterator() {
    return new OrderCopier(tempTableName, sourceDao, tickRate);
  }

  public OrderDAO getOrderDao() {
    return destDao;
  }
}


class BatchInsertIterator implements Iterator<LocalDateTime> {
  private Iterator<Set<Order>> orderIterator;
  private OrderDAO destDao;

  public BatchInsertIterator(OrderBatch orderBatch, OrderDAO destDao) {
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
