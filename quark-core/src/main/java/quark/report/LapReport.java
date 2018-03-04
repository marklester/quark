package quark.report;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import quark.algorithms.Algorithm;
import quark.balance.BalanceListing;
import quark.orders.ProcessedOrder;

public class LapReport implements Comparable<LapReport> {

  public static LapReport of(Algorithm algo, LocalDateTime time) {
    return new LapReport(time);
  }

  private final LocalDateTime dateTime;
  private final Set<ProcessedOrder> processedOrders = new ConcurrentSkipListSet<>();
  private final Set<DataPoint> dataPoints = Sets.newConcurrentHashSet();
  private BalanceListing balanceListing;
  private final Variables variables = new Variables();

  public LapReport(LocalDateTime dateTime) {
    this.dateTime = dateTime;
  }

  public Set<ProcessedOrder> getProcessedOrders() {
    return processedOrders;
  }

  public LocalDateTime getDateTime() {
    return dateTime;
  }

  public Set<DataPoint> getDataPoints() {
    return dataPoints;
  }

  public BalanceListing getBalanceListing() {
    return balanceListing;
  }

  public void setBalanceListing(BalanceListing balanceListing) {
    this.balanceListing = balanceListing;
  }

  @Override
  public int compareTo(LapReport other) {
    return dateTime.compareTo(other.getDateTime());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(dateTime);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final LapReport other = (LapReport) obj;
    return Objects.equal(this.dateTime, other.dateTime);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("time", getDateTime())
        .add("total", getBalanceListing().total()).toString();
  }

  public Variables getVariables() {
    return variables;
  }
}
