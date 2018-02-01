package quark;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.opentable.db.postgres.junit.EmbeddedPostgresRules;
import com.opentable.db.postgres.junit.SingleInstancePostgresRule;

import quark.db.OrderDAO;
import quark.db.CockroachDatabaseManager;
import quark.orders.Order;
import quark.orders.Order.OrderType;
import quark.orders.StandardOrder;

public class OrderDAOTest {
  @Rule
  public SingleInstancePostgresRule pg = EmbeddedPostgresRules.singleInstance();

  private OrderDAO dao;

  @Before
  public void setUp() throws SQLException, IOException {
    pg.getEmbeddedPostgres().getPostgresDatabase();
    CockroachDatabaseManager manager =
        new CockroachDatabaseManager(pg.getEmbeddedPostgres().getPostgresDatabase());
    dao = manager.getOrderDao();
  }

  @Test
  public void testInsert() throws Exception {
    int tpId = 1;
    LocalDateTime dt = LocalDateTime.now();
    Order order = new StandardOrder("hash", tpId, OrderType.BUY, "label", new BigDecimal(0),
        new BigDecimal(0), new BigDecimal(0), dt);
    dao.insert(order);
    Set<Order> outOrder = dao.getOrders(tpId);
    Assert.assertNotNull(outOrder);
    Assert.assertEquals(1, outOrder.size());
    Assert.assertEquals("hash", outOrder.stream().findFirst().get().getHash());
  }

  @Test
  public void testBatchInsert() {
    LocalDateTime dt = LocalDateTime.now();
    int tpId = 1;
    Order order = new StandardOrder("hash1", tpId, OrderType.BUY, "label", new BigDecimal(0),
        new BigDecimal(0), new BigDecimal(0), dt);
    Order order2 = new StandardOrder("hash2", tpId, OrderType.BUY, "label", new BigDecimal(0),
        new BigDecimal(0), new BigDecimal(0), dt);
    ArrayList<Order> orders = Lists.newArrayList(order, order2);
    dao.insert(orders);
    Set<Order> outOrders = dao.getOrders(tpId);
    Assert.assertNotNull(outOrders);
    Assert.assertEquals(2, outOrders.size());
  }

  @Test
  public void testDropDupsOnBatchInsert() {
    LocalDateTime dt = LocalDateTime.now();
    int tpId = 1;
    Order order = new StandardOrder("hash1", tpId, OrderType.BUY, "label", new BigDecimal(0),
        new BigDecimal(0), new BigDecimal(0), dt);
    Order order2 = new StandardOrder("hash1", tpId, OrderType.BUY, "label", new BigDecimal(0),
        new BigDecimal(0), new BigDecimal(0), dt);
    Order order3 = new StandardOrder("hash2", tpId, OrderType.BUY, "label", new BigDecimal(0),
        new BigDecimal(0), new BigDecimal(0), dt);
    ArrayList<Order> orders = Lists.newArrayList(order, order2, order3);
    dao.insert(orders);
    Set<Order> outOrders = dao.getOrders(tpId);
    Assert.assertNotNull(outOrders);
    Assert.assertEquals(2, outOrders.size());
  }

  @Test
  public void testGetLastOrder() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    int tpId = 1;

    LocalDateTime dt = LocalDateTime.now();
    LocalDateTime dtfuture = dt.plus(Duration.ofMinutes(10));

    Order order = new StandardOrder("hash1", tpId, OrderType.BUY, "label", new BigDecimal(0),
        new BigDecimal(0), new BigDecimal(0), dt);
    Order order2 = new StandardOrder("hash2", tpId, OrderType.BUY, "label", new BigDecimal(0),
        new BigDecimal(0), new BigDecimal(0), dtfuture);
    dao.insert(order);
    dao.insert(order2);
    Order outOrder = dao.getLastOrderFor(tpId);
    Assert.assertNotNull(outOrder);
    Assert.assertEquals("hash2", outOrder.getHash());
    Assert.assertEquals(dtfuture.format(formatter), outOrder.getTimestamp().format(formatter));
  }

  @Test
  public void testGetLastOrderDate() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    int tpId = 1;

    LocalDateTime dt = LocalDateTime.now();
    LocalDateTime dtfuture = dt.plus(Duration.ofMinutes(10));

    Order order = new StandardOrder("hash1", tpId, OrderType.BUY, "label", new BigDecimal(0),
        new BigDecimal(0), new BigDecimal(0), dt);
    Order order2 = new StandardOrder("hash2", tpId, OrderType.BUY, "label", new BigDecimal(0),
        new BigDecimal(0), new BigDecimal(0), dtfuture);
    dao.insert(order);
    dao.insert(order2);
    LocalDateTime lastDate = dao.getLastOrderDate();
    Assert.assertNotNull(lastDate);
    Assert.assertEquals(dtfuture.format(formatter), lastDate.format(formatter));
  }

  @Test
  public void testGetOrderRange() {
    int tpId = 1;

    LocalDateTime dtstart = LocalDateTime.now();
    LocalDateTime dtmid = dtstart.plus(Duration.ofMinutes(5));
    LocalDateTime dtmid2 = dtstart.plus(Duration.ofMinutes(6));
    LocalDateTime dtend = dtstart.plus(Duration.ofMinutes(10));

    Order order = new StandardOrder("hash1", tpId, OrderType.BUY, "label", new BigDecimal(0),
        new BigDecimal(0), new BigDecimal(0), dtstart);
    Order order2 = new StandardOrder("hash2", tpId, OrderType.BUY, "label", new BigDecimal(0),
        new BigDecimal(0), new BigDecimal(0), dtmid);
    Order order3 = new StandardOrder("hash3", tpId, OrderType.BUY, "label", new BigDecimal(0),
        new BigDecimal(0), new BigDecimal(0), dtend);
    dao.insert(Arrays.asList(order, order2, order3));
    Set<Order> outOrders = dao.getOrdersFrom(dtstart, dtmid2);
    Assert.assertNotNull(outOrders);
    Assert.assertEquals(2, outOrders.size());
    Assert.assertFalse(
        outOrders.stream().filter(o -> order.getHash().equals("hash3")).findAny().isPresent());
  }

  @Test
  public void testMovingAverage() {
    int tpId = 1;
    LocalDateTime time = LocalDateTime.now();
    BigDecimal sum = new BigDecimal(0);
    int range = 10;
    for (int i = 0; i < range; i++) {
      BigDecimal d = new BigDecimal(i);
      sum = sum.add(d);
      StandardOrder order = new StandardOrder("hash" + i, tpId, OrderType.BUY, "", d, d, d,
          time.minus(Duration.ofMinutes(1)));
      dao.insert(order);
    }
    BigDecimal avg = sum.divide(new BigDecimal(10));
    BigDecimal avgFromDb = dao.getAvg(tpId, Duration.ofHours(1));
    Assert.assertThat(avgFromDb, Matchers.comparesEqualTo(avg));
  }
}
