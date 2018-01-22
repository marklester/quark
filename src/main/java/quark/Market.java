package quark;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;

import quark.model.MonetaryAmount;
import quark.model.StandardMoney;
/**
 * {
    "TradePairId": 100,
    "Label": "LTC/BTC",
    "AskPrice": 0.00006000,
    "BidPrice": 0.02000000,
    "Low": 0.00006000,
    "High": 0.00006000,
    "Volume": 1000.05639978,
    "LastPrice": 0.00006000,
    "BuyVolume": 34455.678,
    "SellVolume": 67003436.37658233,
    "Change": -400.00000000,
    "Open": 0.00000500,
    "Close": 0.00000600,
    "BaseVolume": 3.58675866,
    "BaseBuyVolume": 11.25364758,
    "BaseSellVolume": 3456.06746543
}
 */
public class Market {
  private JsonNode node;
  private TradePair tradePair;

  public Market(JsonNode node, TradePairManager tradePairManager) {
    this.node = node;
    this.tradePair = tradePairManager.getTradePair(node.get("TradePairId").asLong());
  }

  public TradePair getTradePair() {
    return tradePair;
  }
  public MonetaryAmount getAskPrice() {
    BigDecimal value = new BigDecimal(node.get("AskPrice").asText());
    return new StandardMoney(value, getTradePair().getSymbol());
  }

  public MonetaryAmount getBidPrice() {
    return new StandardMoney(node.get("BidPrice").decimalValue(), getTradePair().getBaseSymbol());
  }

  public String getLabel() {
    return node.get("Label").asText();
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("label", getLabel()).add("ask", getAskPrice())
        .add("bid", getBidPrice()).toString();
  }
}
