package quark.algorithms;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import quark.model.Balance;
import quark.model.Market;
import quark.model.TradePair;
import quark.orders.AlgoOrder;
import quark.orders.Order.OrderType;
import quark.orders.ProcessedOrder;
import quark.trader.Trader;

public class MovingAverageAlgo implements Algorithm {
  private static Logger LOGGER = LoggerFactory.getLogger(MovingAverageAlgo.class);
  private Map<Integer, BigDecimal> shortAvgs;
  private Map<Integer, BigDecimal> longAvgs;
  private TreeMap<SMA, AlgoOrder> buys = Maps.newTreeMap();
  private TreeMap<SMA, AlgoOrder> sells = Maps.newTreeMap(Collections.reverseOrder());
  private LocalDateTime currentTime;
  private Duration shortDuration;
  private Duration longDuration;

  public MovingAverageAlgo(Duration shortDuration, Duration longDuration) {
    this.shortDuration = shortDuration;
    this.longDuration = longDuration;
  }

  @Override
  public void init(LocalDateTime currentTime, Trader trader) throws Exception {
    this.currentTime = currentTime;

    LocalDateTime firstOrder = trader.getOrderDao().getFirstOrderDate();
    if (currentTime.minus(longDuration).compareTo(firstOrder) < 0) {
      throw new IllegalStateException("not enough history for run algo");
    }
    shortAvgs = trader.getOrderDao().getAllAvg(currentTime, shortDuration);
    longAvgs = trader.getOrderDao().getAllAvg(currentTime, longDuration);
    buys.clear();
    sells.clear();
  }

  @Override
  public void apply(Market market, Trader trader) throws Exception {
    TradePair tradePair = market.getTradePair();

    BigDecimal oneDayAvg = shortAvgs.getOrDefault(tradePair.getId(), BigDecimal.ZERO);
    BigDecimal threeDayAvg = longAvgs.getOrDefault(tradePair.getId(), BigDecimal.ZERO);
    SMA sma = new SMA(oneDayAvg, threeDayAvg);

    if (canBuy(trader, market) && sma.isValid()
        && sma.getShortAvg().compareTo(sma.getLongAvg()) > 0) {
      // if trending up buy
      buys.put(sma, new AlgoOrder(currentTime, .1, tradePair, OrderType.BUY, sma));

    } else if (canSell(trader, market) && sma.isValid()
        && sma.getShortAvg().compareTo(sma.getLongAvg()) < 0) {
      // if trending down sell
      // TODO add open orders logic
      sells.put(sma, new AlgoOrder(currentTime, 1, tradePair, OrderType.SELL, sma));
    }
  }

  @Override
  public Set<ProcessedOrder> executeOrders(Trader trader) {
    Set<AlgoOrder> orders = Sets.newLinkedHashSet(buys.values());
    orders.addAll(sells.values());
    LinkedHashSet<ProcessedOrder> processed = Sets.newLinkedHashSet();
    for (AlgoOrder order : orders) {
      try {
        processed.add(trader.execute(order));
      } catch (Exception e) {
        LOGGER.trace("Could not complete trade {}", order, e);
      }
    }
    return processed;
  }

  private boolean canBuy(Trader trader, Market market) throws Exception {
    Balance baseBalance =
        trader.getBalanceManager().getBalance(market.getTradePair().getBaseCurrency());
    BigDecimal minBase = market.getTradePair().getMinimumBaseTrade();
    BigDecimal baseBalanceAvailable = baseBalance.getAvailable();
    return baseBalanceAvailable.compareTo(BigDecimal.ZERO) > 0
        && baseBalanceAvailable.compareTo(minBase) > 0;
  }

  boolean canSell(Trader trader, Market market) throws Exception {
    Balance holdingBalance =
        trader.getBalanceManager().getBalance(market.getTradePair().getCurrency());

    BigDecimal minCoin = market.getTradePair().getMinimumTrade();
    return holdingBalance.getAvailable().compareTo(BigDecimal.ZERO) > 0
        && holdingBalance.getAvailable().compareTo(minCoin) > 0;
  }
}
