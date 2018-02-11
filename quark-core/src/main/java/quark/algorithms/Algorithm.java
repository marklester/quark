package quark.algorithms;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import quark.charts.PlotlyTrace;
import quark.model.Market;
import quark.orders.ProcessedOrder;
import quark.trader.Trader;

public interface Algorithm {
  void apply(Market market, Trader trader) throws Exception;

  Set<ProcessedOrder> executeOrders(Trader trader);

  default void init(LapReport lapReport, Trader trader) throws Exception {

  }

  default Map<CoinKey, PlotlyTrace> getData() {
    return Collections.emptyMap();
  }
}
