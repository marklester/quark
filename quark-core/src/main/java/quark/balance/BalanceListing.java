package quark.balance;

import java.math.BigDecimal;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quark.CryptopiaCurrency;
import quark.model.Balance;

public interface BalanceListing {
  public static Logger LOGGER = LoggerFactory.getLogger(BalanceListing.class);

  Balance getBalance(int currencyID);

  Balance getBalance(CryptopiaCurrency currency);

  Collection<Balance> getBalances();

  int size();

  default String summary() {
    StringBuilder builder = new StringBuilder();
    builder.append("SUMMARY\n");
    BigDecimal sum = BigDecimal.ZERO;
    for (Balance balance : getBalances()) {
      BigDecimal value = null;
      try {
        value = balance.toUSD();
        sum = sum.add(value);
      } catch (Exception e) {
        LOGGER.error("could not get usd value for {}", balance.getSymbol(), e);
      }
      String bStr = String.format("balance: %s amount:%s  value: $%s\n", balance.getSymbol(),
          balance.getAvailable(), value);
      builder.append(bStr);
    }
    builder.append("total: " + sum);
    return builder.toString();
  }

  default BigDecimal total() {
    BigDecimal sum = BigDecimal.ZERO;
    for (Balance balance : getBalances()) {
      BigDecimal value = null;
      try {
        value = balance.toUSD();
        sum = sum.add(value);
      } catch (Exception e) {
        LOGGER.error("could not get usd value for {}", balance.getSymbol(), e);
      }
    }
    return sum;
  }

  BalanceListing snapshot();
}
