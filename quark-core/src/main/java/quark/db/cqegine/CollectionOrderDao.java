package quark.db.cqegine;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
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
import com.googlecode.cqengine.resultset.ResultSet;
import com.googlecode.cqengine.stream.StreamFactory;

import quark.db.OrderDAO;
import quark.model.IPriceRange;
import quark.model.MutablePriceRange;
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
  private static final Attribute<Order, BigDecimal> PRICE = QueryFactory.attribute(Order::getPrice);

  public CollectionOrderDao() {
    this.backingOrders = new ConcurrentIndexedCollection<>();
    backingOrders.addIndex(NavigableIndex.onAttribute(TRADE_PAIR_ID));
    backingOrders.addIndex(NavigableIndex.onAttribute(ORDER_TYPE));
    backingOrders.addIndex(NavigableIndex.onAttribute(ORDER_DATE));
    backingOrders.addIndex(NavigableIndex.onAttribute(PRICE));
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

  @Override
  public Map<String, Integer> countOrdersBy(String dtpattern) {
    DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern(dtpattern);
    return backingOrders.stream().map(o -> dtFormatter.format(o.getTimestamp()))
        .collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(e -> 1)));
  }

  @Override
  public Integer getOrderCount() {
    return backingOrders.size();
  }

  @Override
  public Map<Integer, ? extends IPriceRange> getPriceRanges(LocalDateTime anchor,
      Duration overTime) {
    LocalDateTime past = anchor.minus(overTime);
    long end = anchor.toEpochSecond(ZoneOffset.UTC);
    long start = past.toEpochSecond(ZoneOffset.UTC);
    Between<Order, Long> query = QueryFactory.between(ORDER_DATE, start, end);
    ResultSet<Order> results = backingOrders.retrieve(query);
    Map<Integer, MutablePriceRange> ranges = new HashMap<>();
    for (Order order : results) {
      ranges.compute(order.getTradePairId(),
          new BiFunction<Integer, MutablePriceRange, MutablePriceRange>() {

            @Override
            public MutablePriceRange apply(Integer key, MutablePriceRange current) {
              BigDecimal price = order.getPrice();
              if (current == null) {
                return new MutablePriceRange(key, price, price);
              }
              current.computeHigh(price);
              current.computeLow(price);
              return current;
            }
          });
    }
    return ranges;
  }

  @Override
  public Map<Integer, Order> getLastOrders() {
    Map<Integer, Order> lastOrders = Maps.newHashMap();
    try (ResultSet<Order> orders = backingOrders.retrieve(QueryFactory.all(Order.class),
        QueryFactory.queryOptions(QueryFactory.orderBy(QueryFactory.descending(ORDER_DATE))))) {
      for (Order o : orders) {
        lastOrders.compute(o.getTradePairId(), new BiFunction<Integer, Order, Order>() {

          @Override
          public Order apply(Integer key, Order current) {
            if (current == null) {
              return o;
            }
            if (o.getTimestamp().isAfter(current.getTimestamp())) {
              return o;
            }
            return current;
          }
        });
      }
    }
    return lastOrders;
  }

  @Override
  public void setTable(Table<Record> table) {
    // TODO Auto-generated method stub
    
  }
}
