package quark.orders;

import java.math.BigDecimal;

import com.google.common.base.MoreObjects;

import quark.CryptopiaCurrency;
import quark.model.Balance;

public class Receipt {
  private final CryptopiaCurrency product;
  private final BigDecimal amount;
  private final Balance price;

  public Receipt(CryptopiaCurrency product,BigDecimal amount, Balance price) {
    this.amount = amount;
    this.product = product;
    this.price = price;
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
  
  public String toString() {
    return MoreObjects.toStringHelper(this).add("product", product).add("amount", amount)
        .add("price", price).toString();
  }

}
