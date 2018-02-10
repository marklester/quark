package quark.orders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import quark.model.TradePair;
import quark.orders.Order.OrderType;

public class OrderBuilder {
  public static OrderBuilder of(TradePair tradePair) {
    return new OrderBuilder().tradePair(tradePair);
  }
  
  public static OrderBuilder withHash(String hash) {
    return new OrderBuilder().hash(hash);
  }
  
  private String hash;
  private Integer tpId;
  private OrderType orderType;
  private String label;
  private BigDecimal price;
  private BigDecimal total;
  private BigDecimal amount;
  private LocalDateTime orderDate;

  public OrderBuilder hash(String hash) {
    this.hash = hash;
    return this;
  }

  public OrderBuilder tradePairId(int tpId) {
    this.tpId = tpId;
    return this;
  }

  public OrderBuilder orderType(OrderType orderType) {
    this.orderType = orderType;
    return this;
  }

  public OrderBuilder label(String label) {
    this.label = label;
    return this;
  }

  public OrderBuilder price(BigDecimal price) {
    this.price = price;
    return this;
  }

  public OrderBuilder total(BigDecimal total) {
    this.total = total;
    return this;
  }

  public OrderBuilder amount(BigDecimal amount) {
    this.amount = amount;
    return this;
  }

  public OrderBuilder orderDate(LocalDateTime orderDate) {
    this.orderDate = orderDate;
    return this;
  }

  public OrderBuilder tradePair(TradePair tradePair) {
    return this.tradePairId(tradePair.getId()).label(tradePair.getLabel());
  }

  public OrderBuilder setAll(BigDecimal value) {
    return this.price(value).amount(value).total(value);
  }

  public void clear() {
    hash = null;
    tpId = null;
    orderType = null;
    label = null;
    price = null;
    total = null;
    amount = null;
    orderDate = null;
  }

  public Order build() {
    return new StandardOrder(hash, tpId, orderType, label, price, total, amount, orderDate);
  }
}
