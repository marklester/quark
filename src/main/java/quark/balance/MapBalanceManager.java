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

  public MapBalanceManager(CurrencyManager currencyManager) {
    this.currencyManager = currencyManager;
  }

  @Override
  public Balance getBalance(int currencyID) {
    return balances.computeIfAbsent(currencyID, cid -> getDefaultBalance(currencyID));
  }

  Balance getDefaultBalance(int currencyId) {
    CryptopiaCurrency currency = currencyManager.getCurrency(currencyId);
    return new Balance(currency, new BigDecimal(0));
  }

  @Override
  public Collection<Balance> getBalances() {
    return balances.values();
  }

  @Override
  public void setBalance(Balance balance) {
    balances.put(balance.getCurrencyId(), balance);
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
}
