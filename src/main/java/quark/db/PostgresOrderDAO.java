package quark.db;

import static quark.db.OrderFields.AMOUNT;
import static quark.db.OrderFields.ID;
import static quark.db.OrderFields.LABEL;
import static quark.db.OrderFields.ORDER_DATE;
import static quark.db.OrderFields.ORDER_TYPE;
import static quark.db.OrderFields.PRICE;
import static quark.db.OrderFields.TOTAL;
import static quark.db.OrderFields.TRADE_PAIR_ID;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertReturningStep;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import com.google.common.collect.Sets;

import quark.orders.Order;

public class PostgresOrderDAO implements OrderDAO {
  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
  private DSLContext ctx;


  public PostgresOrderDAO(DSLContext ctx) {
    this.ctx = ctx;
  }

  public void insert(Order order) {
    createQuery(order).execute();
  }

  InsertReturningStep<Record> createQuery(Order order) {
    return ctx.insertInto(DSL.table("orders")).set(ID, order.getHash())
        .set(TRADE_PAIR_ID, order.getTradePairId())
        .set(ORDER_DATE, Timestamp.valueOf(order.getTimestamp())).set(LABEL, order.getLabel())
        .set(PRICE, order.getPrice()).set(AMOUNT, order.getAmount()).set(TOTAL, order.getTotal())
        .set(ORDER_TYPE, order.getType().symbol).onConflict(ID).doNothing();
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

  @Override
  public BigDecimal getAvg(int tradePairId, Duration overTime) {
    LocalDateTime ldtNow = LocalDateTime.now();
    LocalDateTime dtFut = ldtNow.minus(overTime);
    Timestamp end = Timestamp.valueOf(ldtNow);
    Timestamp start = Timestamp.valueOf(dtFut);

    Field<Timestamp> dateField = OrderFields.ORDER_DATE;
    Field<Integer> tradePairField = OrderFields.TRADE_PAIR_ID;
    Field<BigDecimal> price = OrderFields.PRICE;

    Record1<BigDecimal> result = ctx.select(DSL.avg(price)).from(DSL.table("orders"))
        .where(tradePairField.eq(tradePairId).and(dateField.between(start, end))).fetchOne();
    return result.value1();
  }

  @Override
  public LocalDateTime getLastOrderDate() {
    Field<Object> dateField = DSL.field("orderdate");
    Record result = ctx.select(dateField).from(DSL.table("orders")).orderBy(dateField.desc())
        .limit(1).fetchOne();
    LocalDateTime date = result.get(DSL.field("orderdate"), LocalDateTime.class);
    return date;
  }

  @Override
  public Set<Order> getOrdersFrom(LocalDateTime start, LocalDateTime end) {
    Field<Object> dateField = DSL.field("orderdate");
    List<Order> result = ctx.selectFrom(DSL.table("orders")).where(dateField.between(start, end))
        .fetch(new OrderRecordMapper());
    return Sets.newHashSet(result);
  }

  @Override
  public Order getLastOrderFor(int tpId) {
    Field<Integer> tradePairField = OrderFields.TRADE_PAIR_ID;
    Order result = ctx.selectFrom(DSL.table("orders")).where(tradePairField.eq(tpId))
        .orderBy(OrderFields.ORDER_DATE.desc()).limit(1).fetchOne(new OrderRecordMapper());
    return result;
  }


  @Override
  public Set<Order> getOrders(int tpId) {
    Field<Integer> tradePairField = OrderFields.TRADE_PAIR_ID;
    List<Order> result = ctx.selectFrom(DSL.table("orders")).where(tradePairField.eq(tpId))
        .orderBy(OrderFields.ORDER_DATE.desc()).fetch(new OrderRecordMapper());
    return Sets.newHashSet(result);
  }
}
