package quark.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class Balances {
  public static List<Balance> convertToBalance(JsonNode response) {
    JsonNode data = response.get("Data");
    List<Balance> balances = new ArrayList<>();
    for (JsonNode node : data) {
      balances.add(new Balance(node));
    }

    return balances;
  }
}