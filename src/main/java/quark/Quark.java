package quark;

import quark.trader.CryptopiaTrader;
import quark.trader.Trader;

public class Quark {
  public static void main(String[] args) throws Exception {
    Trader trader = new CryptopiaTrader();
    trader.getMarketHistory().startPolling();
  }
}
