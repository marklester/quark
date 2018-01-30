package quark.balance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import quark.model.Balance;

public class MapBalanceManager implements BalanceManager{
  Map<Integer,Balance> balances = new HashMap<>();
  
  @Override
  public Balance getBalance(int currencyID) {
    return balances.get(currencyID);
  }

  @Override
  public Collection<Balance> getBalances() {
    return balances.values();
  }

  @Override
  public void updateBalance(Balance balance) {
    // TODO Auto-generated method stub
    
  }
  
}
