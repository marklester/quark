package quark.algorithms;

import java.time.LocalDateTime;
import java.util.Set;

import quark.model.Market;
import quark.orders.ProcessedOrder;
import quark.trader.Trader;

public class StochasticOscillatorAlgo implements Algorithm{
  
  public void init(LocalDateTime currentTime, Trader trader) throws Exception{
//    BigDecimal currentPrice = trader.getOrderDao().getLastOrderFor(1).getPrice();
    //BigDecimal highX = trader.getOrderDao().getHighAvg(currentTime,Duration.ofDays(1),5);    
    //BigDecimal lowX = trader.getOrderDao().getHighAvg(currentTime,Duration.ofDays(1),5);    
  }
  
  @Override
  public void apply(Market market, Trader trader) throws Exception {

  }

  @Override
  public Set<ProcessedOrder> executeOrders(Trader trader) {
    // TODO Auto-generated method stub
    return null;
  }

}
