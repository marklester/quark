package quark;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;

import quark.algorithms.Algorithm;
import quark.algorithms.LapReport;
import quark.charts.PlotlyTrace;
import quark.model.Balance;
import quark.model.Market;
import quark.trader.Trader;

public class AlgoRunner {
  private static Logger LOGGER = LoggerFactory.getLogger(AlgoRunner.class);
  private Algorithm algo;
  private Trader trader;
  private Collection<Market> markets;
  private Set<LapReport> reports = Sets.newLinkedHashSet();

  public AlgoRunner(Trader trader, Algorithm algo) throws Exception {
    this.trader = trader;
    this.algo = algo;
    markets = trader.getMarketManager().getMarkets(10000);
  }

  public Optional<LapReport> run(LocalDateTime time) {
    Stopwatch sw = Stopwatch.createStarted();

    try {
      LapReport report = LapReport.of(algo,time);
      algo.init(report, trader);
      LOGGER.debug("applying algorithm to {} markets", markets.size());
      for (Market market : markets) {
        algo.apply(market, trader);
      }
      algo.executeOrders(trader);
      reports.add(report);
      return Optional.of(report);
    } catch (Exception e) {
      LOGGER.error("could not run algo", e);
    }

    LOGGER.info("algo took {} to {}", algo, sw);
    return Optional.absent();
  }

  public Set<LapReport> getReport() {
    return reports;
  }

  public Set<PlotlyTrace> plot() throws Exception {

    Set<PlotlyTrace> vals = Sets.newHashSet();
    for (Balance b : trader.getBalanceManager().getBalances()) {
      String cName = b.getCurrency().getName();
      algo.getData().entrySet().stream().filter(e -> e.getKey().coin.equals(cName))
          .forEach(e -> vals.add(e.getValue()));;
    }
    return vals;
  }
}
