package quark.orders;

import java.math.BigDecimal;

import com.google.common.base.MoreObjects;

import quark.CryptopiaCurrency;
import quark.model.Balance;

public class Receipt {
  private final CryptopiaCurrency product;
  private final BigDecimal amount;
  private final Balance price;
  private final BigDecimal fee;

  public Receipt(CryptopiaCurrency product, BigDecimal amount, Balance price, BigDecimal fee) {
    this.amount = amount;
    this.product = product;
    this.price = price;
    this.fee = fee;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public Balance getPrice() {
    return price;
  }


  public CryptopiaCurrency getProduct() {
    return product;
  }

  public BigDecimal getFee() {
    return fee;
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("product", product).add("amount", amount)
        .add("price", price).toString();
  }

}
