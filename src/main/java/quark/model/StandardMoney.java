package quark.model;

import java.math.BigDecimal;

import com.google.common.base.MoreObjects;

/**
 * represent a money
 */
public class StandardMoney implements MonetaryAmount {
  private final String symbol;
  private final BigDecimal usdAmount;

  public StandardMoney(BigDecimal usdAmount, String symbol) {
    this.usdAmount = usdAmount;
    this.symbol = symbol;
  }

  public String getSymbol() {
    return symbol;
  }

  @Override
  public BigDecimal getValue() {
    return usdAmount;
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("amount(USD)", getValue()).add("symbol", getSymbol())
        .toString();
  }
}
