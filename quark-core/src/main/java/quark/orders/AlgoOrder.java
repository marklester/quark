package quark.orders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.google.common.base.MoreObjects;

import quark.algorithms.SMA;
import quark.model.TradePair;
import quark.orders.Order.OrderType;

public class AlgoOrder implements Comparable<AlgoOrder> {

  private final BigDecimal percentage;
  private final TradePair tradePair;
  private final OrderType orderType;
  private final Decision decision;
  private final LocalDateTime time;

  public AlgoOrder(LocalDateTime time, double percentage, TradePair tradePair, OrderType orderType,
      SMA sma) {
    this.time = time;
    this.percentage = new BigDecimal(percentage);
    this.tradePair = tradePair;
    this.orderType = orderType;
    this.decision = sma;
  }

  public BigDecimal getPercentage() {
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

  @Override
  public int compareTo(AlgoOrder other) {
    return time.compareTo(other.time);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((decision == null) ? 0 : decision.hashCode());
    result = prime * result + ((orderType == null) ? 0 : orderType.hashCode());
    result = prime * result + ((percentage == null) ? 0 : percentage.hashCode());
    result = prime * result + ((time == null) ? 0 : time.hashCode());
    result = prime * result + ((tradePair == null) ? 0 : tradePair.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AlgoOrder other = (AlgoOrder) obj;
    if (decision == null) {
      if (other.decision != null)
        return false;
    } else if (!decision.equals(other.decision))
      return false;
    if (orderType != other.orderType)
      return false;
    if (percentage == null) {
      if (other.percentage != null)
        return false;
    } else if (!percentage.equals(other.percentage))
      return false;
    if (time == null) {
      if (other.time != null)
        return false;
    } else if (!time.equals(other.time))
      return false;
    if (tradePair == null) {
      if (other.tradePair != null)
        return false;
    } else if (!tradePair.equals(other.tradePair))
      return false;
    return true;
  }

}
