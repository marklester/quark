package quark;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;

import quark.algorithms.Algorithm;
import quark.model.Market;
import quark.orders.ProcessedOrder;
import quark.trader.Trader;

public class AlgoRunner {
  private static Logger LOGGER = LoggerFactory.getLogger(AlgoRunner.class);
  private Algorithm algo;
  private Trader trader;
  private Collection<Market> markets;
  private Set<ProcessedOrder> processedOrders = Sets.newLinkedHashSet();
  public AlgoRunner(Trader trader, Algorithm algo) throws Exception {
    this.trader = trader;
    this.algo = algo;
    markets = trader.getMarketManager().getMarkets(10000);
  }

  public Set<ProcessedOrder> run(LocalDateTime time) {
    Stopwatch sw = Stopwatch.createStarted();
    
    try {
      algo.init(time,trader);
      LOGGER.debug("applying algorithm to {} markets", markets.size());
      for (Market market : markets) {
        algo.apply(market, trader);
      }
      Set<ProcessedOrder> porders = algo.executeOrders(trader);
      getProcessedOrders().addAll(porders);
      return porders;
    } catch (Exception e) {
      LOGGER.error("could not run algo", e);
    }

    LOGGER.info("algo took {} to {}", algo, sw);
    return Collections.emptySet();
  }

  public Set<ProcessedOrder> getProcessedOrders() {
    return processedOrders;
  }
}
