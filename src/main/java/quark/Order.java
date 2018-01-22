package quark;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;

/**
 * { "TradePairId": 5719, "Label": "GBX/BTC", "Type": "Buy", "Price": 0.00348538, "Amount": 3.0,
 * "Total": 0.01045614, "Timestamp": 1516578166 }
 */
public class Order {
  private JsonNode node;

  public Order(JsonNode node) {
    this.node = node;
  }

  public long getTradePairId() {
    return node.get("TradePairId").asLong();
  }

  public String getType() {
    return node.get("Type").asText();
  }

  public String getLabel() {
    return node.get("Label").asText();
  }

  public BigDecimal getPrice() {
    return new BigDecimal(node.get("Price").asText());
  }

  public BigDecimal getTotal() {
    return new BigDecimal(node.get("Total").asText());
  }

  public BigDecimal getAmount() {
    return new BigDecimal(node.get("Amount").asText());
  };

  public Date getTimestamp() {
    return Date.from(Instant.ofEpochSecond(node.get("Timestamp").asLong()));
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("label", getLabel()).add("time", getTimestamp())
        .add("type", getType()).add("price", getPrice()).add("total", getTotal())
        .add("amount", getAmount()).toString();
  }
}
