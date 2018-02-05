package quark;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quark.algorithms.MovingAverageAlgo;
import quark.balance.BalanceManager;
import quark.balance.MapBalanceManager;
import quark.db.DatabaseManager;
import quark.db.PostgresDatabaseManager;
import quark.model.Balance;
import quark.model.CoinMarketCapMoney;
import quark.model.MonetaryAmount;
import quark.trader.MockTrader;
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

    BalanceManager balanceManager = new MapBalanceManager(currencyManager, 10);

    MonetaryAmount money = CoinMarketCapMoney.create("bitcoin");
    BigDecimal startingFund =
        new BigDecimal(100).divide(money.getAmount(), 10, RoundingMode.HALF_EVEN);
    LOGGER.info("starting with {}", startingFund);
    CryptopiaCurrency currency = currencyManager.getCurrency("BTC").get();
    Balance balance = new Balance(currency, startingFund);
    balanceManager.putBalance(balance);
    String startSummary = balanceManager.summary();
    MarketSimulator simulator = dbManager.getMarketSimulator(Duration.ofHours(4));

    Trader testTrader = new MockTrader(simulator.getOrderDao(), balanceManager, marketManager);

    AlgoRunner runner = new AlgoRunner(testTrader, new MovingAverageAlgo());
    System.out.println(testTrader.getBalanceManager());
    for (LocalDateTime time : simulator) {
      LOGGER.info("running algo at {}", time);
      runner.run();
    }
    System.out.println("START" + startSummary);
    System.out.println("END" + testTrader.getBalanceManager().summary());
  }
}
