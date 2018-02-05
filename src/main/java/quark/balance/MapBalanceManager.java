package quark.balance;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import quark.CryptopiaCurrency;
import quark.CurrencyManager;
import quark.ParseException;
import quark.model.Balance;
import quark.model.MonetaryAmount;

public class MapBalanceManager implements BalanceManager {
  private static Logger LOGGER = LoggerFactory.getLogger(MapBalanceManager.class);
  private Map<Integer, Balance> balances = new ConcurrentHashMap<>();
  private CurrencyManager currencyManager;
  private int maxBalances = 10;

  public MapBalanceManager(CurrencyManager currencyManager, int maxBalances) {
    this.currencyManager = currencyManager;
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

  public String summary() {
    StringBuilder builder = new StringBuilder();
    builder.append("SUMMARY\n");
    for (Balance balance : getBalances()) {
      BigDecimal value = null;
      try {
        MonetaryAmount ma = balance.toUSD();
        value = ma.getAmount();
      } catch (ParseException e) {
        LOGGER.error("could not get usd value for {}", balance.getSymbol(), e);
      }
      String bStr = String.format("balance: %s amount:%s  value: $%s\n", balance.getSymbol(),
          balance.getAvailable(), value);
      builder.append(bStr);
    }
    return builder.toString();
  }

  @Override
  public int size() {
    return balances.size();
  }
}
