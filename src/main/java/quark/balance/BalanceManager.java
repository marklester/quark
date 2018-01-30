package quark.balance;

import quark.model.Balance;

public interface BalanceManager extends BalanceListing {
  void updateBalance(Balance balance);
}
