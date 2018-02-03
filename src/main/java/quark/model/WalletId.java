package quark.model;

public class WalletId {
  private final SimpleCurrency currency;
  private final String label;
  private final String address;
  
  public WalletId(SimpleCurrency currency, String label, String address) {
    this.currency = currency;
    this.label = label;
    this.address = address;
  }

  public SimpleCurrency getCurrency() {
    return currency;
  }

  public String getLabel() {
    return label;
  }

  public String getAddress() {
    return address;
  }
}
