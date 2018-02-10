package quark.orders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface Order {
  public enum OrderType {
    BUY(0), SELL(1), UNKNOWN(-1), ALL(-2);
    public int symbol;

    OrderType(int symbol) {
      this.symbol = symbol;
    }

    public static OrderType parse(String typeStr) {
      switch (typeStr) {
        case "Sell":
          return SELL;
        case "Buy":
          return BUY;
        case "All":
          return ALL;
        default:
          return OrderType.UNKNOWN;
      }
    }

    public static OrderType parse(long typeStr) {
      switch ((int) typeStr) {
        case 1:
          return SELL;
        case 0:
          return BUY;
        case -2:
          return ALL;
        default:
          return OrderType.UNKNOWN;
      }
    }
  }

  public int getTradePairId();

  public OrderType getType();

  public String getLabel();

  public BigDecimal getPrice();

  public BigDecimal getTotal();

  public BigDecimal getAmount();

  public LocalDateTime getTimestamp();

  public String getHash();
}
