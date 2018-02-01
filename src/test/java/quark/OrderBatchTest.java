package quark;

import java.time.Duration;

import org.mockito.Mockito;

import quark.db.OrderDAO;

public class OrderBatchTest {
  public void testOrderBatch(){    
    OrderDAO orderDao = Mockito.mock(OrderDAO.class);
    OrderBatch obatch = new OrderBatch(orderDao, Duration.ofMinutes(10));
  }
}
