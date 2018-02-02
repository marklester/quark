package quark.algorithms;

import java.math.BigDecimal;
import java.time.Duration;

import quark.db.OrderDAO;
import quark.model.Market;
import quark.model.TradePair;
import quark.trader.Trader;

public class MovingAverageAlgo implements Algorithm {

  @Override
  public void apply(Market market, Trader trader) {
    TradePair tpId = market.getTradePair();
    OrderDAO orderDao = trader.getOrderDao();
    BigDecimal oneDayAvg = orderDao.getAvg(tpId.getId(), Duration.ofDays(1));
    BigDecimal threeDayAvg = orderDao.getAvg(tpId.getId(), Duration.ofDays(3));

    if (oneDayAvg.compareTo(threeDayAvg) > 0) {
      // if no open orders
      trader.buy(tpId, .10);
    } else if (oneDayAvg.compareTo(threeDayAvg) < 0) {
      // if no open orders
      trader.sell(tpId, 1);
    }
  }

}
