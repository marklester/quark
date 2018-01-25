package quark.model;

import com.fasterxml.jackson.databind.JsonNode;

public class Balance {
  private JsonNode currencyNode;

  public Balance(JsonNode currencyNode) {
    this.currencyNode = currencyNode;
  }

  public String getCurrencyId() {
    return currencyNode.get("CurrencyId").asText();
  }

  public String getSymbol() {
    return currencyNode.get("Symbol").asText();
  }

  public Double getAvailable() {
    return currencyNode.get("Available").asDouble();
  }

  public String toString() {
    return currencyNode.toString();
  }
}
