package quark.algorithms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import com.google.common.base.MoreObjects;

import quark.orders.Decision;

public class SMA implements Comparable<SMA>, Decision {
  private final BigDecimal shortAvg;
  private final BigDecimal longAvg;
  private final BigDecimal diff;
  private final BigDecimal percentChange;

  public SMA(BigDecimal shortAvg, BigDecimal longAvg) {
    this.shortAvg = shortAvg;
    this.longAvg = longAvg;

    diff = shortAvg.subtract(longAvg);
    if (!diff.equals(BigDecimal.ZERO)) {
      this.percentChange = diff.divide(longAvg, 8, RoundingMode.HALF_UP);
    }else {
      this.percentChange= null;
    }
  }

  BigDecimal getPercentChange() {
    return percentChange;
  }

  BigDecimal getDiff() {
    return diff;
  }

  public BigDecimal getShortAvg() {
    return shortAvg;
  }

  public BigDecimal getLongAvg() {
    return longAvg;
  }

  @Override
  public int hashCode() {
    return Objects.hash(shortAvg, longAvg);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final SMA other = (SMA) obj;
    return Objects.equals(shortAvg, other.getShortAvg())
        && Objects.equals(longAvg, other.getLongAvg());
  }

  @Override
  public int compareTo(SMA other) {
    return getPercentChange().compareTo(other.getPercentChange());
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("%change", percentChange).add("short", shortAvg).add("long", longAvg)
        .add("diff", diff).toString();
  }

  public boolean isValid() {
    if(shortAvg.equals(BigDecimal.ZERO)||longAvg.equals(BigDecimal.ZERO)) {
      return false;
    }
    if(percentChange==null) {
      return false;
    }
    return true;
  }
}
