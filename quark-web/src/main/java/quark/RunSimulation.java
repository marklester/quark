package quark;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import quark.algorithms.LapReport;
import quark.algorithms.MovingAverageAlgo;
import quark.algorithms.Parameter;
import quark.algorithms.SimulationReport;
import quark.balance.BalanceManager;
import quark.balance.MapBalanceManager;
import quark.db.DatabaseManager;
import quark.model.Balance;
import quark.model.MonetaryAmount;
import quark.simulation.MarketSimulator;
import quark.trader.MockTrader;
import quark.trader.Trader;

public class RunSimulation implements Runnable {
  private static Logger LOGGER = LoggerFactory.getLogger(RunSimulation.class);
  private CurrencyManager currencyManager;
  private DatabaseManager dbManager;
  private MarketManager marketManager;
  private SimulationReport report;

  public RunSimulation(SimulationReport report, CurrencyManager currencyManager,
      DatabaseManager dbManager, MarketManager marketManager) {
    this.currencyManager = currencyManager;
    this.dbManager = dbManager;
    this.marketManager = marketManager;
    this.report = report;
  }

  @Override
  public void run() {
    try {
      Integer pSize = Integer.parseInt(report.getParams().get(Parameter.PORTFOLIO_SIZE));
      BalanceManager balanceManager = new MapBalanceManager(currencyManager, pSize);
      MonetaryAmount money = currencyManager.getCurrencyLookup().bySymbol("BTC");
      String startFund = report.getParams().get(Parameter.STARTING_FUND);
      BigDecimal startingFund = CoinMath.divide(new BigDecimal(startFund), money.getValue());
      LOGGER.info("starting with {}", startingFund);
      CryptopiaCurrency currency = currencyManager.getCurrency("BTC").get();
      Balance balance = new Balance(currency, startingFund);
      balanceManager.putBalance(balance);

      String startSummary = balanceManager.summary();
      Duration tickRate = Duration.parse(report.getParams().get(Parameter.TICK_RATE));
      Duration shortAvg = Duration.parse(report.getParams().get(Parameter.SHORT_AVG));
      Duration longAvg = Duration.parse(report.getParams().get(Parameter.LONG_AVG));
      
      MarketSimulator simulator = dbManager.getMarketSimulator(tickRate);
      Trader testTrader = new MockTrader(simulator.getOrderDao(), balanceManager, marketManager);

      AlgoRunner runner = new AlgoRunner(testTrader, new MovingAverageAlgo(shortAvg, longAvg));
      for (LocalDateTime time : simulator) {
        Optional<LapReport> lapReport = runner.run(time);
        LOGGER.info("algo lap report:{}", lapReport);
        if (lapReport.isPresent()) {
          report.addLapReport(lapReport.get());
        }

      }
      report.complete();
      LOGGER.info("START" + startSummary);
      LOGGER.info("END" + testTrader.getBalanceManager().summary());
    } catch (Exception e) {
      LOGGER.error("error with simulation", e);
    }
  }
}
