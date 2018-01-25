package quark.orders;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.InsertReturningStep;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

public class OrderDAO {
  private DSLContext ctx;

  public OrderDAO(DSLContext ctx) {
    this.ctx = ctx;
  }

  public void insert(Order order) {
    createQuery(order).execute();
  }

  InsertReturningStep<Record> createQuery(Order order) {
    return ctx.insertInto(DSL.table("orders")).set(DSL.field("id"), order.getHash())
        .set(DSL.field("tradePairId"), order.getTradePairId())
        .set(DSL.field("orderDate"), formatter.format(order.getTimestamp()))
        .set(DSL.field("label"), order.getLabel()).set(DSL.field("price"), order.getPrice())
        .set(DSL.field("amount"), order.getAmount()).set(DSL.field("total"), order.getTotal())
        .set(DSL.field("orderType"), order.getType().symbol);
  }

  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  InsertReturningStep<Record> createBatchQuery(Order order) {
    return ctx.insertInto(DSL.table("orders"), DSL.field("id"), DSL.field("tradePairId"),
        DSL.field("orderDate"), DSL.field("label"), DSL.field("price"), DSL.field("amount"),
        DSL.field("total"), DSL.field("orderType")).values(order.getHash(), order.getTradePairId(),
            formatter.format(order.getTimestamp()), order.getLabel(), order.getPrice(),
            order.getTotal(), order.getAmount(), order.getType().symbol);
  }

  public void insert(Collection<Order> orders) {
    try {
      List<InsertReturningStep<Record>> queries =
          orders.stream().map(order -> createQuery(order)).collect(Collectors.toList());
      ctx.batch(queries).execute();
    } catch (DataAccessException dae) {
      ((SQLException) dae.getCause()).getNextException().printStackTrace();;
      throw dae;
    }

  }

  public LocalDateTime getLastOrder() {
    Record result = ctx.selectFrom(DSL.table("orders")).orderBy(DSL.field(DSL.field("orderDate")).desc()).limit(1).fetchOne();
    Date date = result.get(DSL.field("orderDate"), Date.class);
    return LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
  }
}
