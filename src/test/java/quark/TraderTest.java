package quark;

import org.junit.Test;

public class TraderTest {
  
  @Test
  public void testGetBalance() throws Exception {
    Trader trader = new Trader();
    trader.getBalance().stream().filter(b -> b.getAvailable() > 0)
        .forEach(b -> System.out.println(b));    
  }
  
  @Test
  public void testGetMarkets() throws Exception {
    Trader trader = new Trader();
    System.out.println(trader.getMarketManager().getMarkets());
  }
  
  @Test
  public void testGetTradePairs() throws Exception {
    Trader trader = new Trader();
    System.out.println(trader.getTradePairManager().getTradePairs());
  }
}
