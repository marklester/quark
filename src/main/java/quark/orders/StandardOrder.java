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
public class StandardOrder implements Order {
  

  private final String hash;
  private final long tradePairId;
  private final OrderType type;
  private final String label;
  private final BigDecimal price;
  private final BigDecimal total;
  private final BigDecimal amount;
  private final LocalDateTime timestamp;

  public StandardOrder(JsonNode node) {
    tradePairId = node.get("TradePairId").asLong();
    type = OrderType.parse(node.get("Type").asText());
    label = node.get("Label").asText();
    price = new BigDecimal(node.get("Price").asText());
    amount = new BigDecimal(node.get("Amount").asText());
    total = new BigDecimal(node.get("Total").asText());
    timestamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(node.get("Timestamp").asLong()),
        ZoneOffset.UTC);
    hash = DigestUtils.sha256Hex(node.toString());
  }

  public long getTradePairId() {
    return tradePairId;
  }

  public OrderType getType() {
    return type;
  }

  public String getLabel() {
    return label;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public BigDecimal getTotal() {
    return total;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public String getHash() {
    return hash;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((amount == null) ? 0 : amount.hashCode());
    result = prime * result + ((hash == null) ? 0 : hash.hashCode());
    result = prime * result + ((label == null) ? 0 : label.hashCode());
    result = prime * result + ((price == null) ? 0 : price.hashCode());
    result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
    result = prime * result + ((total == null) ? 0 : total.hashCode());
    result = prime * result + (int) (tradePairId ^ (tradePairId >>> 32));
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    StandardOrder other = (StandardOrder) obj;
    if (amount == null) {
      if (other.amount != null)
        return false;
    } else if (!amount.equals(other.amount))
      return false;
    if (hash == null) {
      if (other.hash != null)
        return false;
    } else if (!hash.equals(other.hash))
      return false;
    if (label == null) {
      if (other.label != null)
        return false;
    } else if (!label.equals(other.label))
      return false;
    if (price == null) {
      if (other.price != null)
        return false;
    } else if (!price.equals(other.price))
      return false;
    if (timestamp == null) {
      if (other.timestamp != null)
        return false;
    } else if (!timestamp.equals(other.timestamp))
      return false;
    if (total == null) {
      if (other.total != null)
        return false;
    } else if (!total.equals(other.total))
      return false;
    if (tradePairId != other.tradePairId)
      return false;
    if (type != other.type)
      return false;
    return true;
  }
  
  public String toString() {
    return MoreObjects.toStringHelper(this).add("hash", getHash()).add("label", getLabel())
        .add("time", getTimestamp()).add("type", getType()).add("price", getPrice())
        .add("total", getTotal()).add("amount", getAmount()).toString();
  }

}
