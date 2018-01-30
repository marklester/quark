package quark.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class Currency {
  private final String name;
  private final String symbol;

  public Currency(String name, String symbol) {
    this.name = name;
    this.symbol = symbol;
  }

  public String getName() {
    return name;
  }

  public String getSymbol() {
    return symbol;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, symbol);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Currency other = (Currency) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (symbol == null) {
      if (other.symbol != null)
        return false;
    } else if (!symbol.equals(other.symbol))
      return false;
    return true;
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("name", getName()).add("symbol", getSymbol())
        .toString();
  }
}
