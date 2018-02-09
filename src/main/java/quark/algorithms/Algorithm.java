package quark.algorithms;

import java.time.LocalDateTime;
import java.util.Set;

import quark.model.Market;
import quark.orders.ProcessedOrder;
import quark.trader.Trader;

public interface Algorithm {
  void apply(Market market, Trader trader) throws Exception;

  Set<ProcessedOrder> executeOrders(Trader trader);

  default void init(LocalDateTime currentTime, Trader trader) throws Exception{
    
  }
}
