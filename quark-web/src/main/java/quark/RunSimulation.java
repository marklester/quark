package quark;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quark.algorithms.MovingAverageAlgo;
import quark.balance.BalanceManager;
import quark.balance.MapBalanceManager;
import quark.charts.PlotlyTrace;
import quark.db.DatabaseManager;
import quark.model.Balance;
import quark.model.MonetaryAmount;
import quark.orders.ProcessedOrder;
import quark.trader.MockTrader;
import quark.trader.Trader;

public class RunSimulation implements Callable<Set<PlotlyTrace>> {
  private static Logger LOGGER = LoggerFactory.getLogger(RunSimulation.class);
  private CurrencyManager currencyManager;
  private DatabaseManager dbManager;
  private MarketManager marketManager;

  public RunSimulation(CurrencyManager currencyManager, DatabaseManager dbManager,
      MarketManager marketManager) {
    this.currencyManager = currencyManager;
    this.dbManager = dbManager;
    this.marketManager = marketManager;
  }

  @Override
  public Set<PlotlyTrace> call() throws Exception {
    BalanceManager balanceManager = new MapBalanceManager(currencyManager, 2);

    MonetaryAmount money = currencyManager.getCurrencyLookup().bySymbol("BTC");
    BigDecimal startingFund =
        new BigDecimal(100).divide(money.getValue(), 8, RoundingMode.HALF_EVEN);
    LOGGER.info("starting with {}", startingFund);
    CryptopiaCurrency currency = currencyManager.getCurrency("BTC").get();
    Balance balance = new Balance(currency, startingFund);
    balanceManager.putBalance(balance);
    String startSummary = balanceManager.summary();
    MarketSimulator simulator = dbManager.getMarketSimulator(Duration.ofDays(1));

    Trader testTrader = new MockTrader(simulator.getOrderDao(), balanceManager, marketManager);

    AlgoRunner runner =
        new AlgoRunner(testTrader, new MovingAverageAlgo(Duration.ofDays(1), Duration.ofDays(3)));
    System.out.println(testTrader.getBalanceManager());
    for (LocalDateTime time : simulator) {
      List<ProcessedOrder> porders =
          runner.run(time).stream().filter(o -> o.isSuccess()).collect(Collectors.toList());
      LOGGER.info("{} algo result: {}:trades:{}", time, testTrader.getBalanceManager().total(),
          porders.size());
    }
    runner.getProcessedOrders().stream().filter(o -> o.isSuccess())
        .forEach(o -> LOGGER.info("order:{}", o));
    LOGGER.info("START" + startSummary);
    LOGGER.info("END" + testTrader.getBalanceManager().summary());
    return runner.plot();
  }
}
