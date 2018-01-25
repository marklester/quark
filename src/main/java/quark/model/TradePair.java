package quark.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;

/**
 * { "Id": 4909, "Label": "BTC/USDT", "Currency": "Bitcoin", "Symbol": "BTC", "BaseCurrency":
 * "Tether", "BaseSymbol": "USDT", "Status": "OK", "StatusMessage": null, "TradeFee": 0.2,
 * "MinimumTrade": 1.0E-8, "MaximumTrade": 1.0E8, "MinimumBaseTrade": 1.0, "MaximumBaseTrade":
 * 1.0E8, "MinimumPrice": 1.0E-8, "MaximumPrice": 1.0E8 }
 *
 */
public class TradePair {
  private JsonNode node;

  public TradePair(JsonNode jsonNode) {
    this.node = jsonNode;
  }

  /**
   * the 2nd part of the pair
   * 
   * @return
   */
  public String getBaseSymbol() {
    return node.get("BaseSymbol").asText();
  }

  /**
   * first part of the pair
   * 
   * @return
   */
  public String getSymbol() {
    return node.get("Symbol").asText();
  }
  
  public boolean isClosing(){
    if(node.get("Status").asText().equals("Closing")) {
      return true;
    }
    return false;
  }

  public String getLabel() {
    return node.get("Label").asText();
  }

  public long getId() {
    return node.get("Id").asLong();
  }

  public BigDecimal getMinimumTrade() {
    return node.get("MinimumTrade").decimalValue();
  }

  public BigDecimal getMinimumBaseTrade() {
    return node.get("MinimumBaseTrade").decimalValue();
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", getId()).add("label", getLabel()).toString();
  }
}
