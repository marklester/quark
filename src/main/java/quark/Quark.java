package quark;

import java.time.Duration;
import java.util.Set;

import quark.algorithms.MovingAverageAlgo;
import quark.db.DatabaseManager;
import quark.db.PostgresDatabaseManager;
import quark.orders.Order;
import quark.populator.MarketHistory;
import quark.trader.TestTrader;
import quark.trader.Trader;

public class Quark {

  public static void main(String[] args) throws Exception {
    DatabaseManager dbManager = new PostgresDatabaseManager();
    
//    CurrencyManager currencyManager = new CurrencyManager();
    TradePairManager tradePairManager = TradePairManager.create();
    MarketManager marketManager = new MarketManager(tradePairManager);
    MarketHistory fullMarketHistory = new MarketHistory(dbManager, marketManager);
    fullMarketHistory.startPolling();
//    Trader realTrader =
//        new CryptopiaTrader(dbManager, currencyManager, fullMarketHistory, marketManager);
//    MarketHistory testHistory = new MarketHistory(inMemManager, marketManager);
    Trader testTrader = new TestTrader(dbManager);
    OrderBatch orderBatch = new OrderBatch(dbManager.getOrderDao(), Duration.ofMinutes(15));

    MarketSimulator simulator = dbManager.getMarketSimulator();
    AlgoRunner runner = new AlgoRunner(testTrader, new MovingAverageAlgo());
    for (Set<Order> orders : orderBatch) {
//      inMemManager.getOrderDao().insert(orders);
      runner.run();
    }

  }
}
