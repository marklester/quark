package quark.model;

import java.math.BigDecimal;

public class MutablePriceRange implements IPriceRange {
  private final Integer tpId;
  private BigDecimal high = BigDecimal.ZERO;
  private BigDecimal low = BigDecimal.ZERO;

  public MutablePriceRange(int tpId, BigDecimal high, BigDecimal low) {
    this.tpId = tpId;
    this.high = high;
    this.low = low;
  }

  public BigDecimal getLow() {
    return low;
  }

  public BigDecimal getHigh() {
    return high;
  }

  public Integer getTpId() {
    return tpId;
  }
  
  public void computeHigh(BigDecimal highCandidate) {
    if(highCandidate.compareTo(high)>0) {
      high=highCandidate;
    }
  }
  
  public void computeLow(BigDecimal lowCandidate) {
    if(lowCandidate.compareTo(low)<0) {
      low=lowCandidate;
    }
  }
}
