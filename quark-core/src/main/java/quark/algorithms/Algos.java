package quark.algorithms;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quark.model.Balance;
import quark.model.Market;
import quark.trader.Trader;

public class Algos {
  private static final Logger LOGGER = LoggerFactory.getLogger(Algos.class);
  
  public static boolean canBuy(Trader trader, Market market) throws Exception {
    Balance baseBalance =
        trader.getBalanceManager().getBalance(market.getTradePair().getBaseCurrency());
    BigDecimal minBase = market.getTradePair().getMinimumBaseTrade();
    BigDecimal baseBalanceAvailable = baseBalance.getAvailable();
    return baseBalanceAvailable.compareTo(BigDecimal.ZERO) > 0
        && baseBalanceAvailable.compareTo(minBase) > 0;
  }

  public static boolean canSell(Trader trader, Market market) throws Exception {
    Balance holdingBalance =
        trader.getBalanceManager().getBalance(market.getTradePair().getCurrency());

    BigDecimal minCoin = market.getTradePair().getMinimumTrade();
    BigDecimal myCoin = holdingBalance.getAvailable();
    if (myCoin.compareTo(BigDecimal.ZERO) > 0) {
      LOGGER.info("my coin:{} min: {}", holdingBalance, market);
      if (myCoin.compareTo(minCoin) > 0) {
        return true;
      }
    }
    return false;
  }
}
