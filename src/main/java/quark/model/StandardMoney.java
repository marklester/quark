package quark.model;

import java.math.BigDecimal;

import com.google.common.base.MoreObjects;

/**
 * represent a money
 */
public class StandardMoney implements MonetaryAmount {
  private final String unit;
  private final BigDecimal amount;

  public StandardMoney(BigDecimal amount, String unit) {
    this.amount = amount;
    this.unit = unit;
  }

  public String getUnit() {
    return unit;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("amount", getAmount()).add("unit", getUnit())
        .toString();
  }
}
