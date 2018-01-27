package quark;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;

/**
 * { "Success":true, "Message":null, "Data":[ { "Id":1, "Name":"Bitcoin", "Symbol":"BTC",
 * "Algorithm":"sha256" "WithdrawFee":0.00010000, "MinWithdraw":0.00040000, "MinBaseTrade":0.0,
 * "IsTipEnabled":false, "MinTip":0.0, "DepositConfirmations":6, "Status":"Maintenance",
 * "StatusMessage":"Unable to sync network", "ListingStatus": "Active" }, { ... } ] }
 *
 */
public class CryptopiaCurrency {
  private final int id;
  private final String name;
  private final String symbol;
  private final String status;
  private final String statusMessage;
  private final String listingStatus;
  private final BigDecimal minWithDraw;
  private final BigDecimal withdrawFee;
  private final BigDecimal minBaseTrade;

  public CryptopiaCurrency(JsonNode node) {
    this.id = node.get("Id").asInt();
    this.name = node.get("Name").asText();
    this.symbol = node.get("Symbol").asText();
    this.status = node.get("Status").asText();
    this.statusMessage = node.get("StatusMessage").asText();
    this.listingStatus = node.get("ListingStatus").asText();
    this.minWithDraw = new BigDecimal(node.get("MinWithdraw").asText());
    this.minBaseTrade = new BigDecimal(node.get("MinBaseTrade").asText());
    this.withdrawFee = new BigDecimal(node.get("WithdrawFee").asText());
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSymbol() {
    return symbol;
  }

  public String getStatus() {
    return status;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public String getListingStatus() {
    return listingStatus;
  }

  public BigDecimal getMinWithDraw() {
    return minWithDraw;
  }

  public BigDecimal getWithdrawFee() {
    return withdrawFee;
  }

  public BigDecimal getMinBaseTrade() {
    return minBaseTrade;
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).add("name", name).add("symbol", symbol)
        .add("listingStatus", listingStatus).toString();
  }
}
