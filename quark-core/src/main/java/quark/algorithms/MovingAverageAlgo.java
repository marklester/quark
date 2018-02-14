package quark.algorithms;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import quark.charts.PlotlyTrace.PlotType;
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

  private Map<SMA, AlgoOrder> buys = Maps.newTreeMap();
  private Map<SMA, AlgoOrder> sells = Maps.newTreeMap(Collections.reverseOrder());
  private Set<String> invested = Sets.newHashSet();
  private LocalDateTime currentTime;
  private Duration shortDuration;
  private Duration longDuration;
  private LapReport lapReport;

  public MovingAverageAlgo(Duration shortDuration, Duration longDuration) {
    this.shortDuration = shortDuration;
    this.longDuration = longDuration;
  }

  @Override
  public void init(LapReport lapReport, Trader trader) throws Exception {
    this.lapReport = lapReport;
    this.currentTime = lapReport.getDateTime();

    LocalDateTime firstOrder = trader.getOrderDao().getFirstOrderDate();
    if (this.currentTime.minus(longDuration).compareTo(firstOrder) < 0) {
      throw new IllegalStateException("not enough history for run algo");
    }
    shortAvgs = trader.getOrderDao().getAllAvg(this.currentTime, shortDuration);
    longAvgs = trader.getOrderDao().getAllAvg(this.currentTime, longDuration);

    buys.clear();
    sells.clear();
  }

  @Override
  public void apply(Market market, Trader trader) throws Exception {
    TradePair tradePair = market.getTradePair();

    BigDecimal shortAvg = shortAvgs.getOrDefault(tradePair.getId(), BigDecimal.ZERO);
    plot(market, shortAvg, shortDuration.toString() + " avg");
    BigDecimal longAvg = longAvgs.getOrDefault(tradePair.getId(), BigDecimal.ZERO);
    plot(market, longAvg, longDuration.toString() + " avg");
    SMA sma = new SMA(shortAvg, longAvg);

    if (canBuy(trader, market) && sma.isValid()
        && sma.getShortAvg().compareTo(sma.getLongAvg()) > 0) {
      // if trending up buy
      if (!invested.contains(market.getLabel())) {
        buys.put(sma, new AlgoOrder(currentTime, .1, tradePair, OrderType.BUY, sma));
      }


    } else if (canSell(trader, market) && sma.isValid()
        && sma.getShortAvg().compareTo(sma.getLongAvg()) < 0) {
      // if trending down sell
      // TODO add open orders logic
      sells.put(sma, new AlgoOrder(currentTime, 1, tradePair, OrderType.SELL, sma));
    }
  }

  private void plot(Market market, BigDecimal avg, String label) {
    CoinKey name = new CoinKey(market.getTradePair().getCurrency().getName(), label);
    lapReport.getDataPoints().add(new DataPoint(name, avg, PlotType.scatter));
  }

  private void plot(ProcessedOrder porder) {
    String orderType = porder.getOrder().getOrderType().toString();
    String currency = porder.getOrder().getTradePair().getCurrency().getName();
    CoinKey name = new CoinKey(currency, orderType);
    BigDecimal val = porder.getReceipt().getAmount();
    lapReport.getDataPoints().add(new DataPoint(name, val, PlotType.bar));
  }

  @Override
  public Set<ProcessedOrder> executeOrders(Trader trader) {
    Set<AlgoOrder> orders = Sets.newLinkedHashSet(buys.values());
    orders.addAll(sells.values());
    LinkedHashSet<ProcessedOrder> processed = Sets.newLinkedHashSet();
    for (AlgoOrder order : orders) {
      try {
        ProcessedOrder porder = trader.execute(order);
        if (porder.isSuccess()) {
          plot(porder);
          if (porder.getOrder().getOrderType() == OrderType.BUY) {
            invested.add(porder.getOrder().getTradePair().getLabel());
          } else {
            invested.remove(porder.getOrder().getTradePair().getLabel());
          }
        }
        processed.add(porder);
      } catch (Exception e) {
        LOGGER.trace("Could not complete trade {}", order, e);
        ProcessedOrder forder = ProcessedOrder.failed(order, e.getMessage());
        processed.add(forder);
      }
    }
    
    lapReport.getProcessedOrders().addAll(processed);
    try {
      lapReport.setBalanceListing(trader.getBalanceManager().snapshot());
    } catch (Exception e) {
      LOGGER.error("could get balance manager", e);
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
    BigDecimal myCoin = holdingBalance.getAvailable();
    if (myCoin.compareTo(BigDecimal.ZERO) > 0) {
      LOGGER.info("my coin:{} min: {}", holdingBalance, market);
      if (myCoin.compareTo(minCoin) > 0) {
        return true;
      }
    }
    return false;
  }
}
