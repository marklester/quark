package quark.algorithms;

import java.math.BigDecimal;
import java.time.Duration;

import quark.db.OrderDAO;
import quark.model.Market;
import quark.trader.Trader;

public class MovingAverageAlgo implements Algorithm {

  @Override
  public void apply(Market market, Trader trader) {
    int tpId = market.getTradePair().getId();
    OrderDAO orderDao = trader.getDBManager().getOrderDao();
    BigDecimal oneDayAvg = orderDao.getAvg(tpId, Duration.ofDays(1));
    BigDecimal threeDayAvg = orderDao.getAvg(tpId, Duration.ofDays(3));

    if (oneDayAvg.compareTo(threeDayAvg) > 0) {
      // if no open orders
      trader.order(tpId, .10);
    } else if (oneDayAvg.compareTo(threeDayAvg) < 0) {
      // if no open orders
      trader.order(tpId, -.10);
    }
  }

}
