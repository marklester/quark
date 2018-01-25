package quark.orders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;

/**
 * { "TradePairId": 5719, "Label": "GBX/BTC", "Type": "Buy", "Price": 0.00348538, "Amount": 3.0,
 * "Total": 0.01045614, "Timestamp": 1516578166 }
 */
public class JsonOrder implements Order{  
  private JsonNode node;
  
  public JsonOrder(JsonNode node) {
    this.node = node;
  }

  public long getTradePairId() {
    return node.get("TradePairId").asLong();
  }

  public OrderType getType() {
    return OrderType.parse(node.get("Type").asText());
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

  public LocalDateTime getTimestamp() {
    Instant instant = Instant.ofEpochSecond(node.get("Timestamp").asLong());
    return LocalDateTime.ofInstant(instant,ZoneOffset.UTC);
  }
  
  public String getHash() {
    return DigestUtils.sha256Hex(node.toString());
  }
  
  public String toString() {
    return MoreObjects.toStringHelper(this).add("label", getLabel()).add("time", getTimestamp())
        .add("type", getType()).add("price", getPrice()).add("total", getTotal())
        .add("amount", getAmount()).toString();
  }
}
