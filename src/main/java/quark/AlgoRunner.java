package quark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quark.algorithms.Algorithm;
import quark.model.Market;
import quark.trader.Trader;

public class AlgoRunner {
  private static Logger LOGGER = LoggerFactory.getLogger(AlgoRunner.class);
  private Algorithm algo;
  private Trader trader;
  public AlgoRunner(Trader trader,Algorithm algo) {
    this.trader = trader;
    this.algo = algo;
  }
  public void run(){
    try {
      for(Market market: trader.getMarketManager().getMarkets()) {
        algo.apply(market, trader);
      }
    } catch (Exception e) {
      LOGGER.error("could not run algo",e);
    }
  }
}
