package quark;

import quark.db.DatabaseManager;
import quark.db.PostgresDatabaseManager;

public class Quark {

  public static void main(String[] args) throws Exception {
    DatabaseManager dbManager = new PostgresDatabaseManager();
    
//    DatabaseManager inMemManager = new InMemoryDatabaseManager();
//    CurrencyManager currencyManager = new CurrencyManager();
    TradePairManager tradePairManager = TradePairManager.create();
    MarketManager marketManager = new MarketManager(tradePairManager);
    MarketHistory fullMarketHistory = new MarketHistory(dbManager, marketManager);
    fullMarketHistory.startPolling();
//    Trader realTrader =
//        new CryptopiaTrader(dbManager, currencyManager, fullMarketHistory, marketManager);
//    MarketHistory testHistory = new MarketHistory(inMemManager, marketManager);
//    Trader testTrader = new TestTrader(testHistory);
//    OrderBatch orderBatch = new OrderBatch(dbManager.getOrderDao(), Duration.ofMinutes(15));
//
//    AlgoRunner runner = new AlgoRunner(testTrader, new MovingAverageAlgo());
//    for (Set<Order> orders : orderBatch) {
//      inMemManager.getOrderDao().insert(orders);
//      runner.run();
//    }

  }
}
