package quark.db.cqegine;

import java.math.BigDecimal;
import java.math.RoundingMode;

import quark.orders.Order;

class BigDecimalAccumulator {
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