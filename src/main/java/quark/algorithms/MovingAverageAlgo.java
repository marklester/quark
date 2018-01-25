package quark.algorithms;

import java.time.Duration;

import quark.Trader;
import quark.model.Market;

public class MovingAverageAlgo implements Algorithm{

  @Override
  public void apply(Market market,Trader trader) {
    long tpId=market.getTradePair().getId();
    
    long oneDayAvg = trader.getAvg(tpId, Duration.ofDays(1));
    long threeDayAvg = trader.getAvg(tpId, Duration.ofDays(3));
    
    if(oneDayAvg > threeDayAvg) {
      //if no open orders
      trader.order(tpId,.10);
    }else if(oneDayAvg<threeDayAvg) {
      //if no open orders
      trader.order(tpId,-.10);
    }
  }

}
