package quark.algorithms;

import java.math.BigDecimal;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import quark.charts.PlotlyTrace.PlotType;

public class DataPoint {
  private final CoinKey label;
  private final BigDecimal value;
  private final PlotType type;

  public DataPoint(CoinKey label, BigDecimal value, PlotType type) {
    this.label = label;
    this.value = value;
    this.type = type;
  }

  public CoinKey getLabel() {
    return label;
  }

  public PlotType getType() {
    return type;
  }

  public BigDecimal getValue() {
    return value;
  }


  @Override
  public int hashCode() {
    return Objects.hashCode(label, getValue(), type);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final DataPoint other = (DataPoint) obj;
    return Objects.equal(this.label, other.label)
        && Objects.equal(this.getValue(), other.getValue()) && Objects.equal(this.type, other.type);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("label", label).add("value", getValue())
        .add("type", type).toString();
  }



}
