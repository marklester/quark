package quark.balance;

import java.util.Collection;

import com.google.common.collect.ImmutableSet;

import quark.CryptopiaCurrency;
import quark.model.Balance;

public class ImmutableBalanceListing implements BalanceListing {
  private ImmutableSet<Balance> balances;

  public ImmutableBalanceListing(Collection<Balance> collection) {
    this.balances = ImmutableSet.copyOf(collection);
  }

  @Override
  public Balance getBalance(int currencyID) {
    return balances.stream().filter(b -> b.getCurrencyId() == currencyID).findFirst().orElse(null);
  }

  @Override
  public Balance getBalance(CryptopiaCurrency currency) {
    return getBalance(currency.getId());
  }

  @Override
  public ImmutableSet<Balance> getBalances() {
    return balances;
  }

  @Override
  public int size() {
    return balances.size();
  }

  @Override
  public BalanceListing snapshot() {
    return this;
  }

}
