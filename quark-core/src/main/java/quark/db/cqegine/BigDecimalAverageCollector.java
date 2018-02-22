package quark.db.cqegine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import quark.db.cqegine.BigDecimalAverageCollector.BigDecimalAccumulator;
import quark.orders.Order;

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