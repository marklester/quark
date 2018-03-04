package quark.orders;

import java.math.BigDecimal;

import com.google.common.base.MoreObjects;

import quark.model.Balance;

public class Receipt {
  private final Balance product;
  private final Balance price;
  private final BigDecimal unitPrice;
  private final BigDecimal fee;

  public Receipt(Balance product, Balance price, BigDecimal unitPrice, BigDecimal fee) {
    this.unitPrice = unitPrice;
    this.product = product;
    this.price = price;
    this.fee = fee;
  }

  public Balance getPrice() {
    return price;
  }


  public Balance getProduct() {
    return product;
  }

  public BigDecimal getFee() {
    return fee;
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("product", product).add("price", price).toString();
  }

  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

}
