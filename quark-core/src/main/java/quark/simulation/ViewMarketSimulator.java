package quark.simulation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;

import org.jooq.DSLContext;

import quark.db.DateOrderedIterator;
import quark.db.OrderDAO;
import quark.db.OrderRecordMapper;
import quark.db.PostgresOrderDAO;
import quark.db.ReadOnlyOrderDAO;

public class ViewMarketSimulator implements MarketSimulator{
  private Duration tickRate;
  private OrderDAO destDao;

  public ViewMarketSimulator(DSLContext ctx, Duration tickRate, OrderDAO sourceDao) {
    this.tickRate = tickRate;
    destDao = new PostgresOrderDAO(ctx, new OrderRecordMapper(), sourceDao.getTable());
  }

  public void prepare() {

  }

  @Override
  public Iterator<LocalDateTime> iterator() {
    return new DateOrderedIterator(destDao, tickRate);
  }

  public ReadOnlyOrderDAO getOrderDao() {
    return destDao;
  }
}
