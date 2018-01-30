package quark.balance;

import java.util.Collection;

import quark.model.Balance;

public interface BalanceListing {
  public Balance getBalance(int currencyID);
  public Collection<Balance> getBalances();
}
