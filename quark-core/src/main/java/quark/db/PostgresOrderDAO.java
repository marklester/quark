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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertReturningStep;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.RecordMapper;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.Table;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;

import quark.model.PriceRange;
import quark.orders.Order;
import quark.orders.Order.OrderType;

public class PostgresOrderDAO implements OrderDAO {
  private static Logger LOGGER = LoggerFactory.getLogger(PostgresOrderDAO.class);  
  private DSLContext ctx;
  private RecordMapper<Record, Order> mapper;
  private Table<Record> table = OrderFields.ORDERS;

  public PostgresOrderDAO(DSLContext ctx, RecordMapper<Record, Order> mapper, Table<Record> table) {
    this(ctx, mapper);
    this.table = table;
  }

  public PostgresOrderDAO(DSLContext ctx, RecordMapper<Record, Order> mapper) {
    this.ctx = ctx;
    this.mapper = mapper;
  }

  @Override
  public void insert(Order order) {
    createQuery(order).execute();
  }

  InsertReturningStep<Record> createQuery(Order order) {
    return ctx.insertInto(table).set(ID, order.getHash()).set(TRADE_PAIR_ID, order.getTradePairId())
        .set(ORDER_DATE, Timestamp.valueOf(order.getTimestamp())).set(LABEL, order.getLabel())
        .set(PRICE, order.getPrice()).set(AMOUNT, order.getAmount()).set(TOTAL, order.getTotal())
        .set(ORDER_TYPE, order.getType().symbol).onConflict(ID).doNothing();
  }
  
  @Override
  public void insert(Collection<Order> orders) {
    try {
      LOGGER.debug("inserting {} records", orders.size());
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

    Record1<BigDecimal> result =
        ctx.select(DSL.avg(price)).from(table).where(tradePairField.eq(tradePairId)
            .and(dateField.greaterOrEqual(start).and(dateField.lessOrEqual(end)))).fetchOne();
    return MoreObjects.firstNonNull(result.value1(), new BigDecimal(0));
  }

  @Override
  public Map<Integer, BigDecimal> getAllAvg(LocalDateTime anchor, Duration overTime) {
    LocalDateTime past = anchor.minus(overTime);
    Timestamp end = Timestamp.valueOf(anchor);
    Timestamp start = Timestamp.valueOf(past);

    Field<Timestamp> dateField = OrderFields.ORDER_DATE;
    Field<BigDecimal> price = OrderFields.PRICE;

    Result<Record2<Integer, BigDecimal>> result =
        ctx.select(OrderFields.TRADE_PAIR_ID, DSL.avg(price)).from(table)
            .where(dateField.greaterOrEqual(start).and(dateField.lessOrEqual(end)))
            .groupBy(OrderFields.TRADE_PAIR_ID).fetch();
    Map<Integer, BigDecimal> map =
        result.stream().collect(Collectors.toMap(k -> k.value1(), v -> v.value2()));
    return MoreObjects.firstNonNull(map, Collections.emptyMap());
  }

  @Override
  public LocalDateTime getLastOrderDate() {
    Order result =
        ctx.selectFrom(table).orderBy(OrderFields.ORDER_DATE.desc()).limit(1).fetchOne(mapper);
    if (result == null) {
      return null;
    }
    return result.getTimestamp();
  }

  @Override
  public LocalDateTime getFirstOrderDate() {
    Order result =
        ctx.selectFrom(table).orderBy(OrderFields.ORDER_DATE.asc()).limit(1).fetchOne(mapper);
    if (result == null) {
      return null;
    }
    return result.getTimestamp();
  }

  @Override
  public Set<Order> getOrdersFrom(LocalDateTime start, LocalDateTime end) {
    Field<Timestamp> dateField = OrderFields.ORDER_DATE;
    List<Order> result = ctx.selectFrom(table)
        .where(dateField.between(Timestamp.valueOf(start), Timestamp.valueOf(end)))
        .fetch(new OrderRecordMapper());
    return Sets.newHashSet(result);
  }

  @Override
  public Order getLastOrderFor(int tpId) {
    Field<Integer> tradePairField = OrderFields.TRADE_PAIR_ID;
    Order result = ctx.selectFrom(table).where(tradePairField.eq(tpId))
        .orderBy(OrderFields.ORDER_DATE.desc()).limit(1).fetchOne(new OrderRecordMapper());
    return result;
  }


  @Override
  public Set<Order> getOrders(int tpId) {
    Field<Integer> tradePairField = OrderFields.TRADE_PAIR_ID;
    List<Order> result = ctx.selectFrom(table).where(tradePairField.eq(tpId))
        .orderBy(OrderFields.ORDER_DATE.desc()).fetch(mapper);
    return Sets.newHashSet(result);
  }

  @Override
  public Stream<Order> getOrders() {
    return ctx.selectFrom(table).stream().map(r -> mapper.map(r));
  }

  @Override
  public int getOrderCount(int tpId, OrderType type) {
    SelectConditionStep<Record1<Integer>> query =
        ctx.selectCount().from(table).where(OrderFields.TRADE_PAIR_ID.eq(tpId));
    if (type == OrderType.BUY || type == OrderType.SELL) {
      query = query.and(OrderFields.ORDER_TYPE.eq(type.symbol));
    }
    return query.fetchOne().value1();
  }

  @Override
  public DSLContext getContext() {
    return ctx;
  }

  @Override
  public Table<Record> getTable() {
    return table;
  }

  @Override
  public Map<String, Integer> countOrdersBy(String pattern) {
    String fieldStr = String.format("TO_CHAR(%s,'%s')", OrderFields.ORDER_DATE.getName(), pattern);
    Field<String> timeGrouping = DSL.field(fieldStr, String.class).as("timegroup");
    Map<String, Integer> map =
        ctx.select(timeGrouping, DSL.count()).from(table).groupBy(timeGrouping).fetch()
            .stream().collect(Collectors.toMap(r -> r.value1(), r -> r.value2()));
    return map;
  }

  @Override
  public Integer getOrderCount() {
    return ctx.selectCount().from(table).fetchOne().value1();
  }

  @Override
  public Map<Integer, Order> getLastOrders() {
    // SELECT DISTINCT ON (tradepairid) tradepairid,orderdate,* FROM orders ORDER BY tradepairid,
    // orderdate DESC;
    List<Order> result = ctx.select(OrderFields.ALL).distinctOn(OrderFields.TRADE_PAIR_ID)
        .from(table).orderBy(OrderFields.TRADE_PAIR_ID, OrderFields.ORDER_DATE.desc())
        .fetch(new OrderRecordMapper());
    return result.stream().collect(Collectors.toMap(o -> o.getTradePairId(), o -> o));
  }


  @Override
  public Map<Integer, PriceRange> getPriceRanges(LocalDateTime anchor, Duration overTime) {
    LocalDateTime past = anchor.minus(overTime);
    Timestamp end = Timestamp.valueOf(anchor);
    Timestamp start = Timestamp.valueOf(past);

    Field<Timestamp> dateField = OrderFields.ORDER_DATE;
    Field<BigDecimal> price = OrderFields.PRICE;

    Result<Record3<Integer, BigDecimal, BigDecimal>> result =
        ctx.select(OrderFields.TRADE_PAIR_ID, DSL.max(price), DSL.min(price)).from(table)
            .where(dateField.greaterOrEqual(start).and(dateField.lessOrEqual(end)))
            .groupBy(OrderFields.TRADE_PAIR_ID).fetch();
    Map<Integer, PriceRange> map = result.stream().collect(
        Collectors.toMap(k -> k.value1(), v -> new PriceRange(v.value1(), v.value2(), v.value3())));
    return MoreObjects.firstNonNull(map, Collections.emptyMap());
  }

  @Override
  public void setTable(Table<Record> table) {
    this.table=table;
  }
}
