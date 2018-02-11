package quark.balance;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import quark.CryptopiaCurrency;
import quark.CurrencyManager;
import quark.model.Balance;

public class MapBalanceManager implements BalanceManager {
  private Map<Integer, Balance> balances = new ConcurrentHashMap<>();
  private CurrencyManager currencyManager;
  private int maxBalances = 10;

  public MapBalanceManager(CurrencyManager currencyManager, int maxBalances) {
    this.currencyManager = currencyManager;
    this.maxBalances = maxBalances;
  }

  @Override
  public Balance getBalance(int currencyID) {
    return balances.getOrDefault(currencyID, getDefaultBalance(currencyID));
  }

  Balance getDefaultBalance(int currencyId) {
    CryptopiaCurrency currency = currencyManager.getCurrency(currencyId);
    return new Balance(currency, BigDecimal.ZERO);
  }

  @Override
  public Collection<Balance> getBalances() {
    return balances.values();
  }

  @Override
  public void putBalance(Balance balance) {
    if (balances.containsKey(balance.getCurrencyId()) || balances.size() < maxBalances) {
      balances.put(balance.getCurrencyId(), balance);
    } else {
      throw new IllegalArgumentException("There is too many balances");
    }
  }

  @Override
  public Balance getBalance(CryptopiaCurrency currency) {
    return getBalance(currency.getId());
  }

  public String toString() {
    ToStringHelper builder = MoreObjects.toStringHelper(this);
    for (Balance b : balances.values()) {
      builder.add(b.getSymbol(), b.getAvailable());
    }
    return builder.toString();
  }

  @Override
  public int size() {
    return balances.size();
  }

  @Override
  public BalanceListing snapshot() {
    return new ImmutableBalanceListing(getBalances());
  }
}
