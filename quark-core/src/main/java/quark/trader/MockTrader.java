package quark.trader;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import quark.CoinMath;
import quark.CryptopiaCurrency;
import quark.MarketManager;
import quark.balance.BalanceManager;
import quark.db.ReadOnlyOrderDAO;
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

  private ReadOnlyOrderDAO orderDao;

  public MockTrader(ReadOnlyOrderDAO orderDAO, BalanceManager balanceManager, MarketManager marketManager) {
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
  public ReadOnlyOrderDAO getOrderDao() {
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
    BigDecimal fee = CoinMath.multiply(amountOfBase, tradePair.getTradeFee());
    if (amountOfBase.compareTo(tradePair.getMinimumBaseTrade()) >= 0) {
      Balance currentBaseBalance = balanceManager.getBalance(baseCurrency);
      Balance updatedBaseBalance = currentBaseBalance.add(amountOfBase).substract(fee);
      balanceManager.putBalance(updatedBaseBalance);
      Balance left = holdingBalance.substract(share);
      balanceManager.putBalance(left);
      
      Balance product = new Balance(baseCurrency,amountOfBase);
      Balance price = new Balance(holdCurrency, share);
      return new ProcessedOrder(openOrder, true,
          new Receipt(product,price ,priceOfBase,fee));
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
    BigDecimal fee = CoinMath.multiply(baseShare, tradePair.getTradeFee());
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
      Balance updatedBaseBalance = baseBalance.substract(baseShare).substract(fee);
      balanceManager.putBalance(updatedBaseBalance);
      
      Balance product = new Balance(tradePair.getCurrency(), boughtAmount);
      Balance price = new Balance(baseBalance.getCurrency(), baseShare);
      Receipt receipt = new Receipt(product,price,priceOfCoin,fee);
      return new ProcessedOrder(openOrder, true, receipt);
    }
  }

}
