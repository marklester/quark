package quark.orders;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertReturningStep;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

public class OrderDAO {
  private DSLContext ctx;
  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  
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
        .set(DSL.field("orderType"), order.getType().symbol).onConflict(DSL.field("id")).doNothing();
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
    Field<Object> dateField = DSL.field("orderdate");
    Record result = ctx.select(dateField).from(DSL.table("orders")).orderBy(dateField.desc())
        .limit(1).fetchOne();
    LocalDateTime date = result.get(DSL.field("orderdate"), LocalDateTime.class);
    return date;
  }
}
