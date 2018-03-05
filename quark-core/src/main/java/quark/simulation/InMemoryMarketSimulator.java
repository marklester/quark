package quark.simulation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;

import quark.db.OrderBatcher;
import quark.db.ReadOnlyOrderDAO;
import quark.db.cqegine.CollectionOrderDao;

public class InMemoryMarketSimulator implements MarketSimulator {
  private Duration tickRate;
  private ReadOnlyOrderDAO sourceDao;
  private CollectionOrderDao destDao = new CollectionOrderDao();

  public InMemoryMarketSimulator(Duration tickRate, ReadOnlyOrderDAO sourceDao) {
    this.tickRate = tickRate;
    this.sourceDao = sourceDao;
  }

  @Override
  public Iterator<LocalDateTime> iterator() {
    return new BatchInsertIterator(new OrderBatcher(sourceDao, tickRate), destDao);
  }

  @Override
  public ReadOnlyOrderDAO getOrderDao() {
    return destDao;
  }
}
