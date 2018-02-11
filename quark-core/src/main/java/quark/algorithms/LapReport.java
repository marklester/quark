package quark.algorithms;

import java.time.LocalDateTime;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import quark.balance.BalanceListing;
import quark.orders.ProcessedOrder;

public class LapReport implements Comparable<LapReport> {

  public static LapReport of(Algorithm algo, LocalDateTime time) {
    return new LapReport(time);
  }

  private LocalDateTime dateTime;
  private Set<ProcessedOrder> processedOrders = Sets.newHashSet();
  private Set<DataPoint> dataPoints = Sets.newHashSet();
  private BalanceListing balanceListing;

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
}
