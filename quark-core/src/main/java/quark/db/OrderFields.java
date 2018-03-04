package quark.db;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Set;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

import com.google.common.collect.ImmutableSet;

public class OrderFields {
  public static final Table<Record> ORDERS = DSL.table("orders");
  public static final Field<String> ID = DSL.field("id", String.class);
  public static final Field<Integer> TRADE_PAIR_ID = DSL.field("tradepairid", int.class);
  public static final Field<Timestamp> ORDER_DATE = DSL.field("orderdate", Timestamp.class);
  public static final Field<String> LABEL = DSL.field("label", String.class);
  public static final Field<BigDecimal> PRICE = DSL.field("price", BigDecimal.class);
  public static final Field<BigDecimal> AMOUNT = DSL.field("amount", BigDecimal.class);
  public static final Field<BigDecimal> TOTAL = DSL.field("total", BigDecimal.class);
  public static final Field<Integer> ORDER_TYPE = DSL.field("ordertype", int.class);
  public static final Set<Field<?>> ALL =
      ImmutableSet.of(ID, TRADE_PAIR_ID, ORDER_DATE, LABEL, PRICE, AMOUNT, TOTAL, ORDER_TYPE);
}
