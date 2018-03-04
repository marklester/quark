package quark.algorithms.so;

import java.math.BigDecimal;

import quark.orders.Decision;

public class KAvg implements Decision {
  private final double kAvg;

  public KAvg(double kAvg) {
    this.kAvg = kAvg;
  }

  public double getkAvg() {
    return kAvg;
  }
  
  public BigDecimal toBigDecimal(){
    return new BigDecimal(kAvg);
  }
  
  @Override
  public String toString() {
    return String.format("kAvg: %s",getkAvg());
  }
}
