package quark;

import java.util.List;

import com.google.common.collect.Lists;

import quark.algorithms.Algorithm;
import quark.algorithms.MovingAverageAlgo;
import quark.model.Market;
import quark.trader.Trader;

public class AlgoRunner {
  public void run(){
    List<Market> markets = Lists.newArrayList();
    Algorithm algo = new MovingAverageAlgo();
    Trader trader = null;
    for(Market market: markets) {
      algo.apply(market, trader);
    }
  }
}
