package quark.report;

public class CoinKey {
  public final String coin;
  public final String label;
  
  public CoinKey(String coin, String label) {
    this.coin = coin;
    this.label = label;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((coin == null) ? 0 : coin.hashCode());
    result = prime * result + ((label == null) ? 0 : label.hashCode());
    return result;
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CoinKey other = (CoinKey) obj;
    if (coin == null) {
      if (other.coin != null)
        return false;
    } else if (!coin.equals(other.coin))
      return false;
    if (label == null) {
      if (other.label != null)
        return false;
    } else if (!label.equals(other.label))
      return false;
    return true;
  }
  
  @Override
  public String toString() {
    return coin+" "+label;
  }
}
