package quark.market.stats;

import java.time.Duration;

public interface MarketStats {
  public long getAvg(long tradePairId, Duration overTime);
  
}
