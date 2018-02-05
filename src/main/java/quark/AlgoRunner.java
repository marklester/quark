package quark;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import quark.algorithms.Algorithm;
import quark.model.Market;
import quark.trader.Trader;

public class AlgoRunner {
  private static Logger LOGGER = LoggerFactory.getLogger(AlgoRunner.class);
  private Algorithm algo;
  private Trader trader;
  private Collection<Market> markets;

  public AlgoRunner(Trader trader, Algorithm algo) throws Exception {
    this.trader = trader;
    this.algo = algo;
    markets = trader.getMarketManager().getMarkets(10000);
  }

  public void run() {
    Stopwatch sw = Stopwatch.createStarted();
    
    try {
      algo.init(trader);
      LOGGER.info("applying algorith to {} markets", markets.size());
      for (Market market : markets) {

        algo.apply(market, trader);
      }
    } catch (Exception e) {
      LOGGER.error("could not run algo", e);
    }

    LOGGER.info("algo took {} to {}", algo, sw);
  }
}
