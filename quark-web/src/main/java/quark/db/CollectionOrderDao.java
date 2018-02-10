package quark.db;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.logical.And;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.Between;
import com.googlecode.cqengine.query.simple.Equal;
import com.googlecode.cqengine.stream.StreamFactory;

import quark.db.BigDecimalAverageCollector.BigDecimalAccumulator;
import quark.orders.Order;
import quark.orders.Order.OrderType;

public class CollectionOrderDao implements OrderDAO {
  private IndexedCollection<Order> backingOrders = new ConcurrentIndexedCollection<>();
  private static final Attribute<Order, Integer> TRADE_PAIR_ID =
      QueryFactory.attribute(Order::getTradePairId);
  private static final Attribute<Order, Long> ORDER_DATE =
      QueryFactory.attribute(o -> o.getTimestamp().toEpochSecond(ZoneOffset.UTC));
  private static final Attribute<Order, OrderType> ORDER_TYPE =
      QueryFactory.attribute(Order::getType);

  public CollectionOrderDao() {
    this.backingOrders = new ConcurrentIndexedCollection<>();
    backingOrders.addIndex(NavigableIndex.onAttribute(TRADE_PAIR_ID));
    backingOrders.addIndex(NavigableIndex.onAttribute(ORDER_TYPE));
    backingOrders.addIndex(NavigableIndex.onAttribute(ORDER_DATE));
  }

  @Override
  public void insert(Order order) {
    backingOrders.add(order);
  }

  @Override
  public void insert(Collection<Order> orders) {
    HashSet<Order> dedup = Sets.newHashSet(orders);
    backingOrders.addAll(dedup);
  }

  @Override
  public BigDecimal getAvg(int tradePairId, Duration overTime) {
    Equal<Order, Integer> query = QueryFactory.equal(TRADE_PAIR_ID, tradePairId);
    return StreamFactory.streamOf(backingOrders.retrieve(query))
        .collect(new BigDecimalAverageCollector());
  }

  @Override
  public LocalDateTime getLastOrderDate() {
    QueryOptions ordering =
        QueryFactory.queryOptions(QueryFactory.orderBy(QueryFactory.descending(ORDER_DATE)));
    Query<Order> query = QueryFactory.all(Order.class);
    Order order = Iterators.getNext(backingOrders.retrieve(query, ordering).iterator(), null);
    return order != null ? order.getTimestamp() : null;
  }

  @Override
  public Set<Order> getOrdersFrom(LocalDateTime start, LocalDateTime end) {
    Between<Order, Long> query = QueryFactory.between(ORDER_DATE,
        start.toEpochSecond(ZoneOffset.UTC), end.toEpochSecond(ZoneOffset.UTC));
    return StreamFactory.streamOf(backingOrders.retrieve(query)).collect(Collectors.toSet());
  }

  @Override
  public Order getLastOrderFor(int tpId) {
    QueryOptions ordering =
        QueryFactory.queryOptions(QueryFactory.orderBy(QueryFactory.descending(ORDER_DATE)));
    Query<Order> query = QueryFactory.equal(TRADE_PAIR_ID, tpId);
    return Iterators.getNext(backingOrders.retrieve(query, ordering).iterator(), null);
  }

  @Override
  public Set<Order> getOrders(int tpId) {
    Equal<Order, Integer> query = QueryFactory.equal(TRADE_PAIR_ID, tpId);
    return StreamFactory.streamOf(backingOrders.retrieve(query)).collect(Collectors.toSet());
  }

  @Override
  public int getOrderCount(int tpId, OrderType type) {
    if (type == OrderType.ALL) {
      return backingOrders.size();
    } else {
      Equal<Order, Integer> tpTerm = QueryFactory.equal(TRADE_PAIR_ID, tpId);
      Equal<Order, OrderType> typeTerm = QueryFactory.equal(ORDER_TYPE, type);
      And<Order> query = QueryFactory.and(tpTerm, typeTerm);
      return backingOrders.retrieve(query).size();
    }
  }

  @Override
  public Stream<Order> getOrders() {
    return backingOrders.stream();
  }

  @Override
  public LocalDateTime getFirstOrderDate() {
    QueryOptions ordering =
        QueryFactory.queryOptions(QueryFactory.orderBy(QueryFactory.ascending(ORDER_DATE)));
    Query<Order> query = QueryFactory.all(Order.class);
    Order order = Iterators.getNext(backingOrders.retrieve(query, ordering).iterator(), null);
    return order != null ? order.getTimestamp() : null;
  }

  @Override
  public DSLContext getContext() {
    throw new NotImplementedException("does make sense here");
  }

  @Override
  public Table<Record> getTable() {
    throw new NotImplementedException("does make sense here");
  }

  @Override
  public Map<Integer, BigDecimal> getAllAvg(LocalDateTime anchor, Duration overTime) {
    LocalDateTime past = anchor.minus(overTime);
    long end = anchor.toEpochSecond(ZoneOffset.UTC);
    long start = past.toEpochSecond(ZoneOffset.UTC);
    Between<Order, Long> query = QueryFactory.between(ORDER_DATE, start, end);
    Stream<Order> results = StreamFactory.streamOf(backingOrders.retrieve(query));

    Map<Integer, BigDecimal> grouped = results
        .collect(Collectors.groupingBy(Order::getTradePairId, new BigDecimalAverageCollector()));
    return grouped;
  }

}


class BigDecimalAverageCollector implements Collector<Order, BigDecimalAccumulator, BigDecimal> {

  @Override
  public Supplier<BigDecimalAccumulator> supplier() {
    return BigDecimalAccumulator::new;
  }

  @Override
  public BiConsumer<BigDecimalAccumulator, Order> accumulator() {
    return BigDecimalAccumulator::add;
  }

  @Override
  public BinaryOperator<BigDecimalAccumulator> combiner() {
    return BigDecimalAccumulator::combine;
  }

  @Override
  public Function<BigDecimalAccumulator, BigDecimal> finisher() {
    return BigDecimalAccumulator::getAverage;
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.emptySet();
  }

  static class BigDecimalAccumulator {
    private BigDecimal sum = BigDecimal.ZERO;

    private BigDecimal count = BigDecimal.ZERO;

    public BigDecimalAccumulator() {

    }

    public BigDecimalAccumulator(BigDecimal sum, BigDecimal count) {
      this.sum = sum;
      this.count = count;
    }

    BigDecimal getAverage() {
      return BigDecimal.ZERO.compareTo(count) == 0 ? BigDecimal.ZERO
          : sum.divide(count, 2, RoundingMode.HALF_EVEN);
    }

    BigDecimalAccumulator combine(BigDecimalAccumulator another) {
      return new BigDecimalAccumulator(sum.add(another.getSum()), count.add(another.getCount()));
    }

    void add(Order other) {
      count = count.add(BigDecimal.ONE);
      sum = sum.add(other.getPrice());
    }

    public BigDecimal getSum() {
      return sum;
    }

    public BigDecimal getCount() {
      return count;
    }

  }

}
