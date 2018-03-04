package quark;

import java.time.LocalDateTime;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import quark.algorithms.Algorithm;
import quark.model.Market;
import quark.report.LapReport;
import quark.report.SimulationReport;
import quark.trader.Trader;

public class AlgoRunner {
  private static Logger LOGGER = LoggerFactory.getLogger(AlgoRunner.class);
  private Algorithm algo;
  private Trader trader;
  private Collection<Market> markets;
  private final SimulationReport report;

  public AlgoRunner(SimulationReport report, Trader trader, Algorithm algo) throws Exception {
    this.trader = trader;
    this.algo = algo;
    this.report = report;
    markets = trader.getMarketManager().getMarkets(10000);
  }

  public LapReport run(LocalDateTime time) {
    Stopwatch sw = Stopwatch.createStarted();
    LapReport lapReport = LapReport.of(algo, time);
    try {
      report.addLapReport(lapReport);
      algo.init(report,lapReport, trader);
      LOGGER.debug("applying algorithm to {} markets", markets.size());
      for (Market market : markets) {
        try {
          algo.apply(market, trader); 
        }catch(Exception e) {
          LOGGER.error("Could not apply algo to {} ",market.getLabel());
        }
      }
      algo.executeOrders(trader);
    } catch (Exception e) {
      LOGGER.error("could not run algo", e);
    }
    LOGGER.info("algo lap took {} to {}", algo, sw);
    return lapReport;
  }
}
