package quark.algorithms.so;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;

import quark.algorithms.Algorithm;
import quark.algorithms.Algorithms;
import quark.charts.PlotlyTrace.PlotType;
import quark.model.IPriceRange;
import quark.model.Market;
import quark.orders.AlgoOrder;
import quark.orders.Order;
import quark.orders.Order.OrderType;
import quark.orders.ProcessedOrder;
import quark.report.CoinKey;
import quark.report.DataPoint;
import quark.report.LapReport;
import quark.report.SimulationReport;
import quark.trader.Trader;

public class StochasticOscillatorAlgo implements Algorithm {
  private static Logger LOGGER = LoggerFactory.getLogger(StochasticOscillatorAlgo.class);
  public static final String K_VALUE = "k-value";

  private LapReport lapReport;
  private LocalDateTime currentTime;
  private Duration hlDuration;
  private Set<AlgoOrder> buys = new TreeSet<>();
  private Set<AlgoOrder> sells = Sets.newTreeSet(Collections.reverseOrder());
  private int periods;
  private SimulationReport simReport;
  private Map<Integer, ? extends IPriceRange> priceRanges;
  private Map<Integer, Order> lastPrices;
  private Set<String> invested = Sets.newHashSet();

  public StochasticOscillatorAlgo(Duration hlDuration, int periods) {
    this.hlDuration = hlDuration;
    this.periods = periods;
  }

  @Override
  public void init(SimulationReport simReport, LapReport lapReport, Trader trader)
      throws Exception {
    this.lapReport = lapReport;
    this.currentTime = lapReport.getDateTime();
    this.simReport = simReport;
    this.priceRanges = trader.getOrderDao().getPriceRanges(currentTime, hlDuration);
    this.lastPrices = trader.getOrderDao().getLastOrders();
    buys.clear();
    sells.clear();
  }

  @Override
  public void apply(Market market, Trader trader) throws Exception {
    int tpid = market.getTradePair().getId();
    Order lastOrder = lastPrices.get(tpid);
    if (lastOrder == null) {
      LOGGER.info("No Orders for {}", market.getLabel());
      return;
    }
    BigDecimal lastPrice = lastOrder.getPrice();
    IPriceRange priceRange = priceRanges.get(tpid);
    if (priceRange == null) {
      LOGGER.info("Could not get price range for:{}", market.getLabel());
      return;
    }
    KValue kValue = new KValue(priceRange, lastPrice);
    try {
      plot(market, new BigDecimal(kValue.getkValue()), market.getLabel() + "K Value");
    } catch (NumberFormatException nfe) {
      LOGGER.error("Could not plot {}", kValue, nfe);
    }

    lapReport.getVariables().putVariable(tpid, K_VALUE, kValue.getkValue());
    if (simReport.getLapReports().size() < periods) {
      LOGGER.error("not enough history need at least {}",periods);
      return;
    }
    KAvg kAvg = createAvg(tpid);
    //LOGGER.info("market:{} kAvg = {}", market.getLabel(),kAvg);
    plot(market, kAvg.toBigDecimal(), market.getLabel() + "K Avg");

    if (Algorithms.canSell(trader, market) && kAvg.getkAvg() >= 80.0) {
      sells.add(new AlgoOrder(currentTime, 1, market.getTradePair(), OrderType.SELL, kAvg));
    }
    if (Algorithms.canBuy(trader, market) && kAvg.getkAvg() <= 20.0&&kAvg.getkAvg() > 0.0) {
      if (!invested.contains(market.getLabel())) {
        buys.add(new AlgoOrder(currentTime, .1, market.getTradePair(), OrderType.BUY, kAvg));
      }

    }
  }

  private void plot(Market market, BigDecimal dataPoint, String label) {
    CoinKey name = new CoinKey(market.getTradePair().getCurrency().getName(), label);
    lapReport.getDataPoints().add(new DataPoint(name, dataPoint, PlotType.scatter));
  }

  private KAvg createAvg(int tpId) {
    double kAvg = simReport.getLapReports().descendingSet().stream()
        .map(lr -> MoreObjects.firstNonNull(lr.getVariables().getVariable(tpId, K_VALUE), 0.0))
        .limit(periods).reduce(0.0, Double::sum);
    if(kAvg==0) {
      return new KAvg(KValue.UN_CHANGED.doubleValue());
    }
    return new KAvg(kAvg / periods);
  }

  private void plot(ProcessedOrder porder) {
    String orderType = porder.getOrder().getOrderType().toString();
    String currency = porder.getOrder().getTradePair().getCurrency().getName();
    CoinKey name = new CoinKey(currency, orderType);
    BigDecimal val = porder.getReceipt().getProduct().getAvailable();
    lapReport.getDataPoints().add(new DataPoint(name, val, PlotType.bar));
  }

  @Override
  public Set<ProcessedOrder> executeOrders(Trader trader) {
    Set<AlgoOrder> orders = Sets.newLinkedHashSet(buys);
    orders.addAll(sells);
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

}
