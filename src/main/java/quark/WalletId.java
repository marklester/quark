package quark;

public class WalletId {
  private final Currency currency;
  private final String label;
  private final String address;
  
  public WalletId(Currency currency, String label, String address) {
    this.currency = currency;
    this.label = label;
    this.address = address;
  }

  public Currency getCurrency() {
    return currency;
  }

  public String getLabel() {
    return label;
  }

  public String getAddress() {
    return address;
  }
}
