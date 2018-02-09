package quark.trader;

import java.math.BigDecimal;
import java.math.MathContext;
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
import quark.orders.AlgoOrder;
import quark.orders.Order;
import quark.orders.Order.OrderType;
import quark.orders.ProcessedOrder;
import quark.orders.Receipt;

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
  public ProcessedOrder execute(AlgoOrder openOrder) {
    if (openOrder.getOrderType() == OrderType.BUY) {
      return executeBuy(openOrder);
    }
    if (openOrder.getOrderType() == OrderType.SELL) {
      return executeSell(openOrder);
    }
    return ProcessedOrder.failed(openOrder, "not a valid order type");
  }

  private ProcessedOrder executeSell(AlgoOrder openOrder) {
    try {
      TradePair tradePair = openOrder.getTradePair();
      Order order = getOrderDao().getLastOrderFor(tradePair.getId());
      Preconditions.checkNotNull(order, "There is no history for " + tradePair);
      Balance holdingBalance = balanceManager.getBalance(tradePair.getCurrency());

      if (holdingBalance.getAvailable().compareTo(BigDecimal.ZERO) == 0) {
        return ProcessedOrder.failed(openOrder, "" + holdingBalance + " is 0");
      }

      BigDecimal portion =
          holdingBalance.getAvailable().multiply(new BigDecimal(openOrder.getPercentage()));

      BigDecimal priceOfBase = order.getPrice();
      BigDecimal amountOfBase = portion.multiply(priceOfBase);

      if (amountOfBase.compareTo(tradePair.getMinimumBaseTrade()) >= 0
          && amountOfBase.compareTo(tradePair.getTradeFee()) > 0) {
        Balance boughtCoin = new Balance(tradePair.getBaseCurrency(), amountOfBase);
        balanceManager.putBalance(boughtCoin);
        Balance left = holdingBalance.substract(portion);
        balanceManager.putBalance(left);
        return new ProcessedOrder(openOrder, true, new Receipt(boughtCoin, left));
      } else {
        LOGGER.debug("could not buy {} amount:{} is less the min of {}", tradePair.getSymbol(),
            amountOfBase, tradePair.getMinimumBaseTrade());
        return ProcessedOrder.failed(openOrder, "less than min and/or fee");
      }
    } catch (ExecutionException e) {
      LOGGER.error("could not get balance", e);
      return ProcessedOrder.failed(openOrder, "could not retrieve balance");
    }
  }

  ProcessedOrder executeBuy(AlgoOrder openOrder) {
    try {
      TradePair tradePair = openOrder.getTradePair();

      Balance cashOnHand = balanceManager.getBalance(tradePair.getBaseCurrency());
      BigDecimal portion = cashOnHand.getAvailable().multiply(
          new BigDecimal(openOrder.getPercentage()), new MathContext(8, RoundingMode.HALF_EVEN));
      
      Order lastOrder = getOrderDao().getLastOrderFor(tradePair.getId());
      BigDecimal priceOfCoin = lastOrder.getPrice();

      BigDecimal amount = portion.divide(priceOfCoin, 8, RoundingMode.HALF_EVEN);
      if (amount.compareTo(tradePair.getMinimumTrade()) < 0) {
        LOGGER.info("could not buy {} amount:{} is less the min of {}", tradePair.getSymbol(),
            amount, tradePair.getMinimumTrade());
        return ProcessedOrder.failed(openOrder, "less than minimum trade");
      } else {
        Balance curBalance = balanceManager.getBalance(tradePair.getCurrency());
        Balance boughtCoin = curBalance.add(amount);
        balanceManager.putBalance(boughtCoin);
        Balance left = cashOnHand.substract(portion);
        balanceManager.putBalance(left);
        Receipt receipt = new Receipt(boughtCoin, left);
        return new ProcessedOrder(openOrder, true, receipt);
      }
    } catch (ExecutionException e) {
      LOGGER.error("could not get balance", e);
      return ProcessedOrder.failed(openOrder, "could not get balance");
    }
  }

  @Override
  public void buy(TradePair tradePair, double percentage) {
    LOGGER.debug("trading for {} with {}% of balance", tradePair.getLabel(), percentage * 100);
    try {
      Order order = getOrderDao().getLastOrderFor(tradePair.getId());
      Balance cashOnHand = balanceManager.getBalance(tradePair.getBaseCurrency());
      BigDecimal portion = cashOnHand.getAvailable().multiply(new BigDecimal(percentage));
      BigDecimal priceOfCoin = order.getPrice();

      BigDecimal amount = portion.divide(priceOfCoin, 4, RoundingMode.HALF_EVEN);
      if (amount.compareTo(tradePair.getMinimumTrade()) < 0) {
        LOGGER.info("could not buy {} amount:{} is less the min of {}", tradePair.getSymbol(),
            amount, tradePair.getMinimumTrade());
      } else {
        Balance boughtCoin = balanceManager.getBalance(tradePair.getCurrency()).add(amount);
        balanceManager.putBalance(boughtCoin);
        balanceManager.putBalance(cashOnHand.substract(portion));
        LOGGER.info("bought {} amount:{} price:{}", tradePair.getLabel(), amount, priceOfCoin);
      }
    } catch (ExecutionException e) {
      LOGGER.error("could not get balance", e);
    }

  }

  @Override
  public void sell(TradePair tradePair, double percentageOfHolding) {

    LOGGER.debug("selling {}% of {}", percentageOfHolding * 100, tradePair);
    try {
      Order order = getOrderDao().getLastOrderFor(tradePair.getId());
      Preconditions.checkNotNull(order, "There is no history for " + tradePair);
      Balance holdingBalance = balanceManager.getBalance(tradePair.getCurrency());

      if (holdingBalance.getAvailable().compareTo(BigDecimal.ZERO) == 0) {
        LOGGER.warn("nothing to sell in {}", tradePair);
        return;
      }

      BigDecimal portion =
          holdingBalance.getAvailable().multiply(new BigDecimal(percentageOfHolding));

      BigDecimal priceOfBase = order.getPrice();
      BigDecimal amountOfBase = portion.multiply(priceOfBase);

      if (amountOfBase.compareTo(tradePair.getMinimumBaseTrade()) >= 0) {
        Balance boughtCoin = new Balance(tradePair.getBaseCurrency(), amountOfBase);
        balanceManager.putBalance(boughtCoin);
        balanceManager.putBalance(holdingBalance.substract(portion));
        LOGGER.debug("selling {} amount:{} price:{}", tradePair.getCurrency().getSymbol(), portion,
            priceOfBase);
      } else {
        LOGGER.debug("could not buy {} amount:{} is less the min of {}", tradePair.getSymbol(),
            amountOfBase, tradePair.getMinimumBaseTrade());
      }
    } catch (ExecutionException e) {
      LOGGER.error("could not get balance", e);
    }

  }

}
