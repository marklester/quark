package quark.model;

import java.math.BigDecimal;

import com.google.common.base.MoreObjects;

public class PriceRange implements IPriceRange {
  private final Integer tpId;
  private final BigDecimal high;
  private final BigDecimal low;

  public PriceRange(int tpId, BigDecimal high, BigDecimal low) {
    this.tpId = tpId;
    this.high = high;
    this.low = low;
  }

  @Override
  public BigDecimal getLow() {
    return low;
  }

  @Override
  public BigDecimal getHigh() {
    return high;
  }

  @Override
  public Integer getTpId() {
    return tpId;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("tpId", tpId).add("high", high).add("low", low)
        .toString();
  }
}
