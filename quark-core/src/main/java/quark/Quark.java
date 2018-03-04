package quark;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quark.algorithms.so.StochasticOscillatorAlgo;
import quark.balance.BalanceManager;
import quark.balance.MapBalanceManager;
import quark.db.DatabaseManager;
import quark.db.PostgresDatabaseManager;
import quark.model.Balance;
import quark.model.CurrencyLookup;
import quark.model.MonetaryAmount;
import quark.report.LapReport;
import quark.report.SimParams;
import quark.report.SimulationReport;
import quark.simulation.MarketSimulator;
import quark.trader.MockTrader;
import quark.trader.Trader;

public class Quark{
  private static final Logger LOGGER = LoggerFactory.getLogger(Quark.class);

  public static void main(String[] args) throws Exception {
    DatabaseManager dbManager = new PostgresDatabaseManager();
    CurrencyLookup currencyLookup = CurrencyLookup.create();
    CurrencyManager currencyManager = new CurrencyManager(currencyLookup);
    TradePairManager tradePairManager = TradePairManager.create(currencyManager);
    MarketManager marketManager = new MarketManager(tradePairManager);
    // Trader realTrader =
    // new CryptopiaTrader(dbManager, currencyManager, fullMarketHistory, marketManager);
    // MarketHistory testHistory = new MarketHistory(inMemManager, marketManager);

    BalanceManager balanceManager = new MapBalanceManager(currencyManager, 10);

    MonetaryAmount money = currencyLookup.bySymbol("BTC");
    BigDecimal startingFund = new BigDecimal(100).divide(money.getValue(), 8, RoundingMode.HALF_EVEN);
    LOGGER.info("starting with {}", startingFund);
    CryptopiaCurrency currency = currencyManager.getCurrency("BTC").get();
    Balance balance = new Balance(currency, startingFund);
    balanceManager.putBalance(balance);
    String startSummary = balanceManager.summary();
    MarketSimulator simulator = dbManager.getMarketSimulator(Duration.ofHours(1));

    Trader testTrader = new MockTrader(simulator.getOrderDao(), balanceManager, marketManager);
    SimulationReport report = new SimulationReport("1", new SimParams());
    AlgoRunner runner = new AlgoRunner(report,testTrader, new StochasticOscillatorAlgo(Duration.ofHours(12),3));
    System.out.println(testTrader.getBalanceManager());
    for (LocalDateTime time : simulator) {
          LapReport result = runner.run(time);
          LOGGER.info("algo lap report:{}",result);
    }
    LOGGER.info("START" + startSummary);
    LOGGER.info("END" + testTrader.getBalanceManager().summary());

  }
}
