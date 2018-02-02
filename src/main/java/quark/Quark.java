package quark;

import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quark.algorithms.MovingAverageAlgo;
import quark.balance.MapBalanceManager;
import quark.db.DatabaseManager;
import quark.db.PostgresDatabaseManager;
import quark.trader.TestTrader;
import quark.trader.Trader;

public class Quark {
  private static final Logger LOGGER = LoggerFactory.getLogger(Quark.class);

  public static void main(String[] args) throws Exception {
    DatabaseManager dbManager = new PostgresDatabaseManager();

    CurrencyManager currencyManager = new CurrencyManager();
    TradePairManager tradePairManager = TradePairManager.create(currencyManager);
    MarketManager marketManager = new MarketManager(tradePairManager);
    // Trader realTrader =
    // new CryptopiaTrader(dbManager, currencyManager, fullMarketHistory, marketManager);
    // MarketHistory testHistory = new MarketHistory(inMemManager, marketManager);


    MarketSimulator simulator = dbManager.getMarketSimulator(Duration.ofMinutes(15));
    Trader testTrader =
        new TestTrader(simulator.getOrderDao(), new MapBalanceManager(), marketManager);

    AlgoRunner runner = new AlgoRunner(testTrader, new MovingAverageAlgo());
    for (LocalDateTime time : simulator) {
      LOGGER.info("running algo at {}", time);
      runner.run();
    }

  }
}
