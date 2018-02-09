package quark.orders;

import java.time.LocalDateTime;

import com.google.common.base.MoreObjects;

import quark.algorithms.SMA;
import quark.model.TradePair;
import quark.orders.Order.OrderType;

public class AlgoOrder {
  private final double percentage;
  private final TradePair tradePair;
  private final OrderType orderType;
  private final Decision decision;
  private final LocalDateTime time;

  public AlgoOrder(LocalDateTime time,double percentage, TradePair tradePair, OrderType orderType, SMA sma) {
    this.time = time;
    this.percentage = percentage;
    this.tradePair = tradePair;
    this.orderType = orderType;
    this.decision = sma;
  }

  public double getPercentage() {
    return percentage;
  }

  public TradePair getTradePair() {
    return tradePair;
  }

  public OrderType getOrderType() {
    return orderType;
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("time", time).add("tp", getTradePair().getLabel())
        .add("type", getOrderType()).add("decision", decision).toString();
  }

  public Decision getDecision() {
    return decision;
  }

  public LocalDateTime getTime() {
    return time;
  }
}
