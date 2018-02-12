package quark.trader;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import quark.CoinMath;
import quark.CryptopiaCurrency;
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
    TradePair tradePair = openOrder.getTradePair();
    CryptopiaCurrency baseCurrency = tradePair.getBaseCurrency();
    CryptopiaCurrency holdCurrency = tradePair.getCurrency();
    Order order = getOrderDao().getLastOrderFor(tradePair.getId());
    Preconditions.checkNotNull(order, "There is no history for " + tradePair);
    Balance holdingBalance = balanceManager.getBalance(holdCurrency);

    if (holdingBalance.getAvailable().compareTo(BigDecimal.ZERO) == 0) {
      return ProcessedOrder.failed(openOrder, "" + holdingBalance + " is 0");
    }
    // 1.6
    BigDecimal share = CoinMath.multiply(holdingBalance.getAvailable(), openOrder.getPercentage());
    // .0004
    BigDecimal priceOfBase = order.getPrice();
    // min.00000008 (1.6*.0004)=.000064
    BigDecimal amountOfBase = CoinMath.multiply(share, priceOfBase);

    if (amountOfBase.compareTo(tradePair.getMinimumBaseTrade()) >= 0) {
      Balance currentBaseBalance = balanceManager.getBalance(tradePair.getBaseCurrency());
      Balance updatedBaseBalance = currentBaseBalance.add(amountOfBase);
      balanceManager.putBalance(updatedBaseBalance);
      Balance left = holdingBalance.substract(share);
      balanceManager.putBalance(left);
      return new ProcessedOrder(openOrder, true,
          new Receipt(baseCurrency, amountOfBase, new Balance(holdCurrency, share)));
    } else {
      LOGGER.info("could not sell {} amount:{} is less the min of {} or {}", tradePair.getSymbol(),
          amountOfBase, tradePair.getMinimumBaseTrade(), tradePair.getTradeFee());
      return ProcessedOrder.failed(openOrder, "less than min and/or fee");
    }
  }

  ProcessedOrder executeBuy(AlgoOrder openOrder) {
    TradePair tradePair = openOrder.getTradePair();

    Balance baseBalance = balanceManager.getBalance(tradePair.getBaseCurrency());
    BigDecimal baseShare = CoinMath.multiply(baseBalance.getAvailable(), openOrder.getPercentage());

    Order lastOrder = getOrderDao().getLastOrderFor(tradePair.getId());
    BigDecimal priceOfCoin = lastOrder.getPrice();

    BigDecimal boughtAmount = CoinMath.divide(baseShare, priceOfCoin);
    if (boughtAmount.compareTo(tradePair.getMinimumTrade()) < 0) {
      LOGGER.info("could not buy {} amount:{} is less the min of {}", tradePair.getSymbol(),
          boughtAmount, tradePair.getMinimumTrade());
      return ProcessedOrder.failed(openOrder, "less than minimum trade");
    } else {
      Balance curBalance = balanceManager.getBalance(tradePair.getCurrency());
      Balance updatedCoinBalance = curBalance.add(boughtAmount);
      balanceManager.putBalance(updatedCoinBalance);
      Balance updatedBaseBalance = baseBalance.substract(baseShare);
      balanceManager.putBalance(updatedBaseBalance);
      Receipt receipt = new Receipt(curBalance.getCurrency(), boughtAmount,
          new Balance(baseBalance.getCurrency(), baseShare));
      return new ProcessedOrder(openOrder, true, receipt);
    }
  }

  @Override
  public void buy(TradePair tradePair, double percentage) {
    LOGGER.debug("trading for {} with {}% of balance", tradePair.getLabel(), percentage * 100);
    Order order = getOrderDao().getLastOrderFor(tradePair.getId());
    Balance cashOnHand = balanceManager.getBalance(tradePair.getBaseCurrency());
    BigDecimal portion = cashOnHand.getAvailable().multiply(new BigDecimal(percentage));
    BigDecimal priceOfCoin = order.getPrice();

    BigDecimal amount = portion.divide(priceOfCoin, 4, RoundingMode.HALF_EVEN);
    if (amount.compareTo(tradePair.getMinimumTrade()) < 0) {
      LOGGER.info("could not buy {} amount:{} is less the min of {}", tradePair.getSymbol(), amount,
          tradePair.getMinimumTrade());
    } else {
      Balance boughtCoin = balanceManager.getBalance(tradePair.getCurrency()).add(amount);
      balanceManager.putBalance(boughtCoin);
      balanceManager.putBalance(cashOnHand.substract(portion));
      LOGGER.info("bought {} amount:{} price:{}", tradePair.getLabel(), amount, priceOfCoin);
    }
  }

  @Override
  public void sell(TradePair tradePair, double percentageOfHolding) {

    LOGGER.debug("selling {}% of {}", percentageOfHolding * 100, tradePair);
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
  }

}