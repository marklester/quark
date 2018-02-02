package quark.balance;

import java.util.Collection;

import quark.CryptopiaCurrency;
import quark.model.Balance;

public interface BalanceListing {
  public Balance getBalance(int currencyID);
  
  public Balance getBalance(CryptopiaCurrency currency);
  
  public Collection<Balance> getBalances();
}
