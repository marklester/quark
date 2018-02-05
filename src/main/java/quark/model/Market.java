package quark.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import quark.TradePairManager;

/**
 * { "TradePairId": 100, "Label": "LTC/BTC", "AskPrice": 0.00006000, "BidPrice": 0.02000000, "Low":
 * 0.00006000, "High": 0.00006000, "Volume": 1000.05639978, "LastPrice": 0.00006000, "BuyVolume":
 * 34455.678, "SellVolume": 67003436.37658233, "Change": -400.00000000, "Open": 0.00000500, "Close":
 * 0.00000600, "BaseVolume": 3.58675866, "BaseBuyVolume": 11.25364758, "BaseSellVolume":
 * 3456.06746543 }
 */
public class Market {
  private TradePair tradePair;
  private String label;
  private double volume;

  public Market(TradePair tradePair, String label, double volume) {
    this.tradePair = tradePair;
    this.label = label;
    this.volume = volume;
  }

  public Market(JsonNode node, TradePairManager tradePairManager) {
    this.tradePair = tradePairManager.getTradePair(node.get("TradePairId").asInt());
    Preconditions.checkNotNull(tradePair,"Could not create trade pair for "+ node);
    this.label = node.get("Label").asText();
    this.volume = node.get("Volume").asDouble();
  }

  public TradePair getTradePair() {
    return tradePair;
  }

  public String getLabel() {
    return label;
  }

  public double getVolume() {
    return volume;
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("label", getLabel()).add("volume", getVolume())
        .add("tradePair", getTradePair()).toString();
  }
}
