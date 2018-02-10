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

import javafx.collections.FXCollections;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
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
  private Map<CoinKey, Series<LocalDateTime, Double>> series = Maps.newHashMap();

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

  private void plot(Market market, BigDecimal oneDayAvg, String label) {
    CoinKey name = new CoinKey(market.getTradePair().getCurrency().getName(), label);
    Series<LocalDateTime, Double> plot = series.computeIfAbsent(name,
        k -> new Series<>(name.toString(), FXCollections.observableArrayList()));
    plot.getData().add(new Data<LocalDateTime, Double>(currentTime, oneDayAvg.doubleValue()));
  }

  private void plot(ProcessedOrder porder) {
    String orderType = porder.getOrder().getOrderType().toString();
    String currency = porder.getOrder().getTradePair().getCurrency().getName();
    CoinKey name = new CoinKey(currency, orderType);
    double val = porder.getReceipt().getIn().getAvailable().doubleValue();

    Series<LocalDateTime, Double> plot = series.computeIfAbsent(name,
        k -> new Series<>(name.toString(), FXCollections.observableArrayList()));

    plot.getData().add(new Data<LocalDateTime, Double>(porder.getOrder().getTime(), val));
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

  public Map<CoinKey, Series<LocalDateTime, Double>> getData() {
    return series;
  }
}
