package quark.orders;

import com.google.common.base.MoreObjects;

import quark.model.Balance;

public class Receipt {
  private final Balance in;
  private final Balance out;

  public Receipt(Balance in, Balance out) {
    this.in = in;
    this.out = out;
  }

  public Balance getIn() {
    return in;
  }

  public Balance getOut() {
    return out;
  }
  public String toString() {
    return MoreObjects.toStringHelper(this).add("in", in).add("out", out).toString();
  }

}
