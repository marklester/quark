package quark.algorithms.so;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

import quark.model.IPriceRange;

public class KValue {
  public static final BigDecimal UN_CHANGED = new BigDecimal(50);
  private static final Logger LOGGER = LoggerFactory.getLogger(KValue.class);
  private final IPriceRange priceRange;
  private final BigDecimal kValue;
  private final BigDecimal last;

  public KValue(IPriceRange priceRange, BigDecimal last) {
    this.priceRange = priceRange;
    BigDecimal high = priceRange.getHigh();
    BigDecimal low = priceRange.getLow();
    if (high.compareTo(BigDecimal.ZERO) <= 0 || low.compareTo(BigDecimal.ZERO) <= 0) {
      LOGGER.warn("KValue for {} is invalid", priceRange);
    }
    this.last = last;
    BigDecimal top = last.subtract(low);
    BigDecimal bottom = high.subtract(low);
    
    try {
      this.kValue = (top.compareTo(BigDecimal.ZERO)==0||bottom.compareTo(BigDecimal.ZERO)==0)?UN_CHANGED:new BigDecimal(100)
          .multiply(top.divide(bottom, new MathContext(2, RoundingMode.HALF_EVEN)));      
    }catch(ArithmeticException ae) {
      LOGGER.error("could not do math pr:{} last:{},top:{},botton:{}",priceRange,last,top,bottom);
      throw ae;
    }

  }

  public double getkValue() {
    return kValue.doubleValue();
  }

  public BigDecimal getLast() {
    return last;
  }

  public IPriceRange getPriceRange() {
    return priceRange;
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("priceRange", priceRange).add("last", last)
        .add("kVal", kValue).toString();
  }
}
