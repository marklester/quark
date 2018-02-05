package quark.algorithms;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quark.model.Balance;
import quark.model.Market;
import quark.model.TradePair;
import quark.trader.Trader;

public class MovingAverageAlgo implements Algorithm {
  private static Logger LOGGER = LoggerFactory.getLogger(MovingAverageAlgo.class);
  private Map<Integer, BigDecimal> oneDays;
  private Map<Integer, BigDecimal> threeDays;

  @Override
  public void init(Trader trader) throws Exception {
    oneDays = trader.getOrderDao().getAllAvg(Duration.ofDays(1));
    threeDays = trader.getOrderDao().getAllAvg(Duration.ofDays(3));
  }

  @Override
  public void apply(Market market, Trader trader) throws Exception {
    TradePair tpId = market.getTradePair();

    BigDecimal oneDayAvg = oneDays.getOrDefault(tpId.getId(), BigDecimal.ZERO);
    BigDecimal threeDayAvg = threeDays.getOrDefault(tpId.getId(), BigDecimal.ZERO);
    if (canBuy(trader, market) && oneDayAvg.compareTo(threeDayAvg) > 0) {
      // if no open orders
      LOGGER.debug("buy {} 1D: {} > 3D: {}", market.getTradePair(), oneDayAvg, threeDayAvg);
      trader.buy(tpId, .10);
    } else if (canSell(trader, market) && oneDayAvg.compareTo(threeDayAvg) < 0) {

      LOGGER.debug("sell {} 1D: {} < 3D: {}", market.getTradePair(), oneDayAvg, threeDayAvg);
      // if no open orders
      trader.sell(tpId, 1);
    }
  }

  private boolean canBuy(Trader trader, Market market) throws Exception {
    Balance baseBalance =
        trader.getBalanceManager().getBalance(market.getTradePair().getBaseCurrency());
    BigDecimal minBase = market.getTradePair().getMinimumBaseTrade();
    BigDecimal baseBalanceAvailable = baseBalance.getAvailable();
    return baseBalanceAvailable.compareTo(BigDecimal.ZERO) > 0
        && baseBalanceAvailable.compareTo(minBase) > 0;
  }

  boolean canSell(Trader trader, Market market) throws Exception {
    Balance holdingBalance =
        trader.getBalanceManager().getBalance(market.getTradePair().getCurrency());

    BigDecimal minCoin = market.getTradePair().getMinimumTrade();
    return holdingBalance.getAvailable().compareTo(minCoin) > 0;
  }
}
