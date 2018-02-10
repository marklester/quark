package quark.db;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.jooq.Record;
import org.jooq.RecordMapper;

import quark.orders.Order;
import quark.orders.Order.OrderType;
import quark.orders.StandardOrder;

public class CockroachRecordMapper implements RecordMapper<Record, Order>{
  
  public Order map(Record record) {
    String id = record.get(OrderFields.ID);
    int tpId = Math.toIntExact(record.get(OrderFields.TRADE_PAIR_ID.getName(),long.class));
    OrderType type = OrderType.parse(record.get(OrderFields.ORDER_TYPE.getName(),long.class));
    String label = record.get(OrderFields.LABEL);
    BigDecimal price = record.get(OrderFields.PRICE);
    BigDecimal total = record.get(OrderFields.TOTAL);
    BigDecimal amount = record.get(OrderFields.AMOUNT);
    LocalDateTime orderDate = record.get(OrderFields.ORDER_DATE).toLocalDateTime();
    return new StandardOrder(id,tpId,type, label, price, total, amount, orderDate);
  }

}
