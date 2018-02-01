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
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertReturningStep;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import quark.orders.Order;

public class PostgresOrderDAO implements OrderDAO {
  private static Logger LOGGER = LoggerFactory.getLogger(PostgresOrderDAO.class);
  
  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
  private DSLContext ctx;
  RecordMapper<Record,Order> mapper;

  public PostgresOrderDAO(DSLContext ctx,RecordMapper<Record, Order> mapper) {
    this.ctx = ctx;
    this.mapper = mapper;
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
      LOGGER.info("inserting {} records",orders.size());
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
    Order result = ctx.selectFrom(DSL.table("orders")).orderBy(OrderFields.ORDER_DATE.desc())
        .limit(1).fetchOne(mapper);
    if(result==null) {
      return null;
    }
    return result.getTimestamp();
  }

  @Override
  public Set<Order> getOrdersFrom(LocalDateTime start, LocalDateTime end) {
    Field<Object> dateField = DSL.field("orderdate");
    List<Order> result = ctx.selectFrom(DSL.table("orders")).where(dateField.between(start, end))
        .fetch(new PGRecordMapper());
    return Sets.newHashSet(result);
  }

  @Override
  public Order getLastOrderFor(int tpId) {
    Field<Integer> tradePairField = OrderFields.TRADE_PAIR_ID;
    Order result = ctx.selectFrom(DSL.table("orders")).where(tradePairField.eq(tpId))
        .orderBy(OrderFields.ORDER_DATE.desc()).limit(1).fetchOne(new PGRecordMapper());
    return result;
  }


  @Override
  public Set<Order> getOrders(int tpId) {
    Field<Integer> tradePairField = OrderFields.TRADE_PAIR_ID;
    List<Order> result = ctx.selectFrom(DSL.table("orders")).where(tradePairField.eq(tpId))
        .orderBy(OrderFields.ORDER_DATE.desc()).fetch(mapper);
    return Sets.newHashSet(result);
  }

  @Override
  public Stream<Order> getOrders() {
    return ctx.selectFrom(DSL.table("orders")).stream().map(r->mapper.map(r));
  }
}
