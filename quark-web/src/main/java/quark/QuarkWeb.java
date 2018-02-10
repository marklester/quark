package quark;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quark.algorithms.MovingAverageAlgo;
import quark.balance.BalanceManager;
import quark.balance.MapBalanceManager;
import quark.db.DatabaseManager;
import quark.db.PostgresDatabaseManager;
import quark.model.Balance;
import quark.model.CurrencyLookup;
import quark.model.MonetaryAmount;
import quark.orders.ProcessedOrder;
import quark.trader.MockTrader;
import quark.trader.Trader;

public class QuarkWeb{
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

    BalanceManager balanceManager = new MapBalanceManager(currencyManager, 2);

    MonetaryAmount money = currencyLookup.bySymbol("BTC");
    BigDecimal startingFund = new BigDecimal(100).divide(money.getValue(), 8, RoundingMode.HALF_EVEN);
    LOGGER.info("starting with {}", startingFund);
    CryptopiaCurrency currency = currencyManager.getCurrency("BTC").get();
    Balance balance = new Balance(currency, startingFund);
    balanceManager.putBalance(balance);
    String startSummary = balanceManager.summary();
    MarketSimulator simulator = dbManager.getMarketSimulator(Duration.ofDays(1));

    Trader testTrader = new MockTrader(simulator.getOrderDao(), balanceManager, marketManager);

    AlgoRunner runner = new AlgoRunner(testTrader, new MovingAverageAlgo(Duration.ofDays(1),Duration.ofDays(3)));
    System.out.println(testTrader.getBalanceManager());
    for (LocalDateTime time : simulator) {
      List<ProcessedOrder> porders =
          runner.run(time).stream().filter(o -> o.isSuccess()).collect(Collectors.toList());
      LOGGER.info("{} algo result: {}:trades:{}", time, testTrader.getBalanceManager().total(),
          porders.size());
    }    
    runner.getProcessedOrders().stream().filter(o -> o.isSuccess())
        .forEach(o -> System.out.println(o));
    System.out.println("START" + startSummary);
    System.out.println("END" + testTrader.getBalanceManager().summary());

  }
}
