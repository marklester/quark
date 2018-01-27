package quark.algorithms;

import java.time.Duration;

import quark.model.Market;
import quark.trader.Trader;

public class MovingAverageAlgo implements Algorithm{

  @Override
  public void apply(Market market,Trader trader) {
    long tpId=market.getTradePair().getId();
    
    long oneDayAvg = trader.getMarketStats().getAvg(tpId, Duration.ofDays(1));
    long threeDayAvg = trader.getMarketStats().getAvg(tpId, Duration.ofDays(3));
    
    if(oneDayAvg > threeDayAvg) {
      //if no open orders
      trader.order(tpId,.10);
    }else if(oneDayAvg<threeDayAvg) {
      //if no open orders
      trader.order(tpId,-.10);
    }
  }

}
