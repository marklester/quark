package quark.trader;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import quark.MarketManager;
import quark.balance.BalanceManager;
import quark.db.OrderDAO;
import quark.model.Balance;
import quark.model.TradePair;
import quark.orders.Order;
import quark.orders.Order.OrderType;

public class MockTrader implements Trader {
  private static Logger LOGGER = LoggerFactory.getLogger(MockTrader.class);

  private BalanceManager balanceManager;


  private MarketManager marketManager;

  private OrderDAO orderDao;

  public MockTrader(OrderDAO orderDAO, BalanceManager balanceManager, MarketManager marketManager) {
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
    LOGGER.info("trading for {} with {}% of balance", tradePair.getLabel(), percentage);
    try {
      Order order = getOrderDao().getLastOrderFor(tradePair.getId());
      Balance cashOnHand = balanceManager.getBalance(tradePair.getBaseCurrency());
      BigDecimal portion = cashOnHand.getAvailable().multiply(new BigDecimal(percentage));
      BigDecimal priceOfCoin = order.getPrice();

      BigDecimal amount = portion.divide(priceOfCoin, 8, RoundingMode.HALF_EVEN);
      if (amount.compareTo(tradePair.getMinimumTrade()) < 0) {
        LOGGER.info("could not buy {} amount:{} is less the min of {}", tradePair.getSymbol(),
            amount, tradePair.getMinimumTrade());
      } else {
        balanceManager.setBalance(cashOnHand.substract(portion));
        Balance boughtCoin = new Balance(tradePair.getCurrency(), amount);
        balanceManager.setBalance(boughtCoin);
        LOGGER.info("bought {} amount:{} price:{}", tradePair.getLabel(), amount, priceOfCoin);
      }
    } catch (ExecutionException e) {
      LOGGER.error("could not get balance", e);
    }

  }

  @Override
  public void sell(TradePair tradePair, double percentageOfHolding) {

    LOGGER.info("selling {}% of {}", percentageOfHolding*100, tradePair);
    try {
      Order order = getOrderDao().getLastOrderFor(tradePair.getId());
      Preconditions.checkNotNull(order,"There is no history for "+tradePair);
      Balance holdingBalance = balanceManager.getBalance(tradePair.getCurrency());
      
      if(holdingBalance.getAvailable().compareTo(BigDecimal.ZERO)==0) {
        LOGGER.warn("nothing to sell in {}",tradePair);
        return;
      }
      
      BigDecimal portion =
          holdingBalance.getAvailable().multiply(new BigDecimal(percentageOfHolding));

      BigDecimal priceOfBase = order.getPrice();
      BigDecimal amountOfBase = portion.multiply(priceOfBase);
      
      if (amountOfBase.compareTo(tradePair.getMinimumBaseTrade()) >= 0) {
        balanceManager.setBalance(holdingBalance.substract(portion));
        Balance boughtCoin = new Balance(tradePair.getBaseCurrency(), amountOfBase);
        balanceManager.setBalance(boughtCoin);
        LOGGER.info("selling {} amount:{} price:{}", tradePair.getCurrency().getSymbol(), portion,
            priceOfBase);
      } else {
        LOGGER.info("could not buy {} amount:{} is less the min of {}", tradePair.getSymbol(),
            amountOfBase, tradePair.getMinimumBaseTrade());
      }
    } catch (ExecutionException e) {
      LOGGER.error("could not get balance", e);
    }

  }

}
