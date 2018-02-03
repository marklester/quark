package quark.algorithms;

import java.math.BigDecimal;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quark.db.OrderDAO;
import quark.model.Balance;
import quark.model.Market;
import quark.model.TradePair;
import quark.trader.Trader;

public class MovingAverageAlgo implements Algorithm {
  private static Logger LOGGER = LoggerFactory.getLogger(MovingAverageAlgo.class);

  @Override
  public void apply(Market market, Trader trader) throws Exception {
    TradePair tpId = market.getTradePair();

    if (canBuy(trader, market) || canSell(trader, market)) {
      OrderDAO orderDao = trader.getOrderDao();
      BigDecimal oneDayAvg = orderDao.getAvg(tpId.getId(), Duration.ofDays(1));
      BigDecimal threeDayAvg = orderDao.getAvg(tpId.getId(), Duration.ofDays(3));
      if (oneDayAvg.compareTo(threeDayAvg) > 0) {
        // if no open orders
        LOGGER.info("buy {} 1D: {} > 3D: {}", market.getTradePair(), oneDayAvg, threeDayAvg);
        trader.buy(tpId, .10);
      } else if (oneDayAvg.compareTo(threeDayAvg) < 0) {

        LOGGER.info("sell {} 1D: {} < 3D: {}", market.getTradePair(), oneDayAvg, threeDayAvg);
        // if no open orders
        trader.sell(tpId, 1);
      }
    }
  }



  private boolean canBuy(Trader trader, Market market) throws Exception {
    BigDecimal baseBalance = trader.getBalanceManager()
        .getBalance(market.getTradePair().getBaseCurrency()).getAvailable();
    BigDecimal minBase = market.getTradePair().getMinimumBaseTrade();
    return baseBalance.compareTo(new BigDecimal(0)) > 0 && baseBalance.compareTo(minBase) > 0;
  }

  boolean canSell(Trader trader, Market market) throws Exception {
    Balance holdingBalance =
        trader.getBalanceManager().getBalance(market.getTradePair().getCurrency());
    BigDecimal minCoin = market.getTradePair().getMinimumTrade();
    return holdingBalance.getAvailable().compareTo(minCoin) > 0;
  }
}
