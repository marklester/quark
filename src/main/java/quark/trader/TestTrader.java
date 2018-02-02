package quark.trader;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quark.MarketManager;
import quark.balance.BalanceManager;
import quark.balance.MapBalanceManager;
import quark.db.OrderDAO;
import quark.model.Balance;
import quark.model.TradePair;
import quark.orders.Order;
import quark.orders.Order.OrderType;

public class TestTrader implements Trader {
  private static Logger LOGGER = LoggerFactory.getLogger(TestTrader.class);

  private BalanceManager balanceManager = new MapBalanceManager();


  private MarketManager marketManager;

  private OrderDAO orderDao;

  public TestTrader(OrderDAO orderDAO, BalanceManager balanceManager, MarketManager marketManager) {
    this.orderDao = orderDAO;
    this.balanceManager = balanceManager;
    this.marketManager = marketManager;
  }

  @Override
  public MarketManager getMarketManager() throws Exception {
    return marketManager;
  }


  public void order(TradePair tradePair, OrderType type, BigDecimal price, BigDecimal amount) {}

  @Override
  public BalanceManager getBalanceManager() throws Exception {
    return balanceManager;
  }

  @Override
  public OrderDAO getOrderDao() {
    return orderDao;
  }

  @Override
  public void buy(TradePair tradePair, double percentage) {
    LOGGER.info("trading for {} with {}% of balance");
    try {
      Order order = getOrderDao().getLastOrderFor(tradePair.getId());
      Balance cashOnHand = balanceManager.getBalance(tradePair.getSellToCurrency());
      BigDecimal portion = cashOnHand.getAvailable().multiply(new BigDecimal(percentage));
      BigDecimal priceOfCoin = order.getPrice();

      BigDecimal amount = portion.divide(priceOfCoin);
      if (amount.compareTo(tradePair.getMinimumTrade()) < 0) {
        LOGGER.info("could not buy {} amount:{} is less the min of {}", tradePair.getSymbol(),
            amount, tradePair.getMinimumTrade());
      }
    } catch (ExecutionException e) {
      LOGGER.error("could not get balance", e);
    }

  }

  @Override
  public void sell(TradePair tpId, double d) {
    // TODO Auto-generated method stub

  }

}
