package quark.db;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Iterator;

import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

public class OrderCopier implements Iterator<LocalDateTime> {
  private static final Logger LOGGER = LoggerFactory.getLogger(OrderCopier.class);
  private LocalDateTime bookMark = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
  private OrderDAO srcDao;
  private Duration batchSize;
  private LocalDateTime bookEnd;
  private String destTable;

  public OrderCopier(String destTable, OrderDAO srcDao, Duration batchSize) {
    this.srcDao = srcDao;
    this.batchSize = batchSize;
    bookMark = srcDao.getFirstOrderDate();
    bookEnd = srcDao.getLastOrderDate();
    this.destTable = destTable;
  }

  @Override
  public boolean hasNext() {
    Stopwatch sw = Stopwatch.createStarted();
    LocalDateTime nextBookMark = bookMark.plus(batchSize);
    String query =
        String.format("INSERT INTO {0} SELECT * FROM {1} where orderdate >= {2} AND orderdate < {3}");
    int result = srcDao.getContext().execute(query, DSL.table(destTable), srcDao.getTable(),
        DSL.val(bookMark), DSL.val(nextBookMark));
    LOGGER.info("next batch success: {}, took {}", result, sw);
    bookMark = nextBookMark;
    if (nextBookMark.compareTo(bookEnd) > 0) {
      return false;
    }
    return true;
  }

  @Override
  public LocalDateTime next() {
    return bookMark;
  }
}
