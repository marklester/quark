package quark;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

class Balances {
  public static List<Balance> convertToBalance(JsonNode response) {
    JsonNode data = response.get("Data");
    List<Balance> balances = new ArrayList<>();
    for (JsonNode node : data) {
      balances.add(new Balance(node));
    }

    return balances;
  }
}

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
