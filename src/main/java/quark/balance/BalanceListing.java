package quark.balance;

import java.math.BigDecimal;
import java.util.Collection;

import quark.CryptopiaCurrency;
import quark.model.Balance;

public interface BalanceListing {
  Balance getBalance(int currencyID);
  
  Balance getBalance(CryptopiaCurrency currency);
  
  Collection<Balance> getBalances();
  
  int size();
  
  String summary();
  
  BigDecimal total();
}
