package quark.populator;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import quark.CurrencyManager;
import quark.MarketManager;
import quark.TradePairManager;
import quark.db.DatabaseManager;
import quark.db.OrderDAO;
import quark.db.PostgresDatabaseManager;
import quark.model.CurrencyLookup;
import quark.model.Market;

public class MarketHistory {
  private static final Logger LOGGER = LoggerFactory.getLogger(MarketHistory.class);
  ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private ExecutorCompletionService<Boolean> executor =
      new ExecutorCompletionService<>(Executors.newFixedThreadPool(7));

  private DatabaseManager dbManager;
  private MarketManager mktManager;

  public MarketHistory(DatabaseManager dbManager, MarketManager mktManager) {
    this.dbManager = dbManager;
    this.mktManager = mktManager;
  }



  public void startPolling() {
    scheduler.scheduleAtFixedRate(() -> {
      try {
        storeOrders();
      } catch (ExecutionException e) {
        LOGGER.error("could not store orders", e);
      }
    }, 0, 20, TimeUnit.MINUTES);
  }



  private void storeOrders() throws ExecutionException {
    LocalDateTime lastOrder = getLastOrderDate();
    OrderDAO orderDao = dbManager.getOrderDao();
    LOGGER.info("Getting orders after: " + lastOrder);
    Stopwatch total = Stopwatch.createStarted();
    Collection<Market> markets = mktManager.getMarkets();
    int count = 0;
    for (Market market : markets) {
      Callable<Boolean> history =
          new GetMarketHistory(orderDao, market, new Position(count, markets.size()), lastOrder);
      executor.submit(history);
      count += 1;
    }

    for (Market market : markets) {
      try {
        executor.take();
      } catch (Exception e) {
        LOGGER.error("could not take", e);
      }
    }
    LOGGER.info("took {}", total.stop());
  }

  private LocalDateTime getLastOrderDate() {
    return dbManager.getOrderDao().getLastOrderDate();
  }

  public static void main(String args[]) throws Exception {
    DatabaseManager dbManager = new PostgresDatabaseManager();
    CurrencyLookup lookup = CurrencyLookup.create();
    CurrencyManager currencyManager = new CurrencyManager(lookup);
    TradePairManager tradePairManager = TradePairManager.create(currencyManager);
    MarketManager marketManager = new MarketManager(tradePairManager);
    MarketHistory fullMarketHistory = new MarketHistory(dbManager, marketManager);
    fullMarketHistory.startPolling();
  }
}
