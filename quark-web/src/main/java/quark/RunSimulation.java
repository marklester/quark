package quark;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quark.algorithms.sma.MovingAverageAlgo;
import quark.balance.BalanceManager;
import quark.balance.MapBalanceManager;
import quark.db.DatabaseManager;
import quark.model.Balance;
import quark.report.LapReport;
import quark.report.SimulationReport;
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
      Integer pSize = report.getParams().getPortfolioSize();
      BalanceManager balanceManager = new MapBalanceManager(currencyManager, pSize);

      for (Entry<String, String> balance : report.getParams().getStartingBalances().entrySet()) {
        String currencySymbol = balance.getKey();
        BigDecimal startAmount = new BigDecimal(balance.getValue());
        CryptopiaCurrency currency = currencyManager.getCurrency(currencySymbol).get();
        Balance coinBalance = new Balance(currency, startAmount);
        balanceManager.putBalance(coinBalance);
      }

      String startSummary = balanceManager.summary();
      Duration tickRate = Duration.parse(report.getParams().getTickRate());
      Duration shortAvg = Duration.parse(report.getParams().getShortAvg());
      Duration longAvg = Duration.parse(report.getParams().getLongAvg());

      MarketSimulator simulator = dbManager.getMarketSimulator(tickRate);
      Trader testTrader = new MockTrader(simulator.getOrderDao(), balanceManager, marketManager);

      AlgoRunner runner = new AlgoRunner(report,testTrader, new MovingAverageAlgo(shortAvg, longAvg));
      for (LocalDateTime time : simulator) {
        LapReport lapReport = runner.run(time);
        LOGGER.info("algo lap report:{}", lapReport);
      }
      report.complete();
      LOGGER.info("START" + startSummary);
      LOGGER.info("END" + testTrader.getBalanceManager().summary());
    } catch (Exception e) {
      LOGGER.error("error with simulation", e);
    }
  }
}
