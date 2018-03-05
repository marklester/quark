package quark.db;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

public class DateOrderedIterator implements Iterator<LocalDateTime> {
  private static final String ORDERVIEW = "orderview_";
  private static final Logger LOGGER = LoggerFactory.getLogger(DateOrderedIterator.class);
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
  private LocalDateTime bookMark = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
  private ReadOnlyOrderDAO srcDao;
  private Duration interval;
  private LocalDateTime bookEnd;
  private DSLContext ctx;

  public DateOrderedIterator(ReadOnlyOrderDAO srcDao, Duration interval) {
    this.ctx = srcDao.getContext();
    this.srcDao = srcDao;
    this.interval = interval;
    bookMark = srcDao.getFirstOrderDate();
    bookEnd = srcDao.getLastOrderDate();
    clearViews();
  }

  private void clearViews() {
    Result<Record1<String>> results =
        ctx.select(DSL.field("table_name", String.class)).from("INFORMATION_SCHEMA.views").fetch();
    for (Record1<String> result : results) {
      String tableName = result.value1();
      if (tableName.startsWith(ORDERVIEW)) {
        LOGGER.info("dropping view:{}", tableName);
        ctx.dropView(tableName).execute();
      }
    }
  }

  @Override
  public boolean hasNext() {
    Stopwatch sw = Stopwatch.createStarted();

    // gen next bookmark
    LocalDateTime nextBookMark = bookMark.plus(interval);

    Field<Timestamp> orderDate = OrderFields.ORDER_DATE;
    Timestamp end = Timestamp.valueOf(nextBookMark);
    Table<Record> viewName = viewName(nextBookMark);
    ctx.dropViewIfExists(viewName).execute();
    ctx.createView(viewName).as(ctx.selectFrom(OrderFields.ORDERS).where(orderDate.lt(end)))
        .execute();
    srcDao.setTable(viewName);
    LOGGER.info("view for {} success: took {}", viewName, sw);
    // move bookmark
    bookMark = nextBookMark;
    if (nextBookMark.compareTo(bookEnd) > 0) {
      ctx.dropViewIfExists(viewName(bookMark));
      return false;
    }
    return true;
  }

  Table<Record> viewName(LocalDateTime dateTime) {
    return DSL.table(ORDERVIEW + dateTime.format(FORMATTER));
  }

  @Override
  public LocalDateTime next() {
    return bookMark;
  }
}
