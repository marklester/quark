package quark;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class CoinMath {
  public static MathContext COIN_MATH_CONTEXT = new MathContext(8, RoundingMode.HALF_EVEN);
  
  public static BigDecimal divide(BigDecimal one,BigDecimal two){
    return one.divide(two,COIN_MATH_CONTEXT);
  }

  public static BigDecimal multiply(BigDecimal one,BigDecimal two){
    return one.multiply(two,COIN_MATH_CONTEXT);
  }
}
