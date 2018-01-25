package quark.algorithms;

import quark.Trader;
import quark.model.Market;

public interface Algorithm {
  void apply(Market market, Trader trader);
}
