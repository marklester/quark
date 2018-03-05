package quark.simulation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import quark.db.OrderCopier;
import quark.db.ReadOnlyOrderDAO;
import quark.db.OrderFields;
import quark.db.OrderRecordMapper;
import quark.db.PostgresOrderDAO;

public class TempTableMarketSimulator implements MarketSimulator{
  private Duration tickRate;
  private DSLContext ctx;
  private String tempTableName;
  private ReadOnlyOrderDAO sourceDao;
  private PostgresOrderDAO destDao;

  public TempTableMarketSimulator(DSLContext ctx, Duration tickRate, ReadOnlyOrderDAO sourceDao) {
    this.tickRate = tickRate;
    this.ctx = ctx;
    this.tempTableName =
        "simorders" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
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

  public ReadOnlyOrderDAO getOrderDao() {
    return destDao;
  }
}
