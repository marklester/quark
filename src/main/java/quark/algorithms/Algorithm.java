package quark.algorithms;

import quark.model.Market;
import quark.trader.Trader;

public interface Algorithm {
  void apply(Market market, Trader trader);
}
