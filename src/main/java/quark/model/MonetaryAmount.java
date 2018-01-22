package quark.model;

import java.math.BigDecimal;

/**
 * represents money
 */
public interface MonetaryAmount {
  String getUnit();

  BigDecimal getAmount();
}
