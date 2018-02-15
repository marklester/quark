package quark;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;

import quark.model.Currency;
import quark.model.CurrencyLookup;

/**
 * { "Success":true, "Message":null, "Data":[ { "Id":1, "Name":"Bitcoin", "Symbol":"BTC",
 * "Algorithm":"sha256" "WithdrawFee":0.00010000, "MinWithdraw":0.00040000, "MinBaseTrade":0.0,
 * "IsTipEnabled":false, "MinTip":0.0, "DepositConfirmations":6, "Status":"Maintenance",
 * "StatusMessage":"Unable to sync network", "ListingStatus": "Active" }, { ... } ] }
 *
 */
public class CryptopiaCurrency implements Currency {
  public static final CryptopiaCurrency UNKNOWN = new CryptopiaCurrency(-1, "UNKOWN", "", "", "",
      "", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
  private final int id;
  private final String name;
  private final String symbol;
  private final String status;
  private final String statusMessage;
  private final String listingStatus;
  private final BigDecimal minWithDraw;
  private final BigDecimal withdrawFee;
  private final BigDecimal minBaseTrade;
  private final BigDecimal usdValue;

  public CryptopiaCurrency(JsonNode node, CurrencyLookup usdLookup) {
    this.id = node.get("Id").asInt();
    this.name = node.get("Name").asText();
    this.symbol = node.get("Symbol").asText();
    this.status = node.get("Status").asText();
    this.statusMessage = node.get("StatusMessage").asText();
    this.listingStatus = node.get("ListingStatus").asText();
    this.minWithDraw = new BigDecimal(node.get("MinWithdraw").asText());
    this.minBaseTrade = new BigDecimal(node.get("MinBaseTrade").asText());
    this.withdrawFee = new BigDecimal(node.get("WithdrawFee").asText());
    this.usdValue = usdLookup.bySymbol(symbol).getValue();
  }

  public CryptopiaCurrency(int id, String name, String symbol, String status, String statusMessage,
      String listingStatus, BigDecimal minWithDraw, BigDecimal withdrawFee, BigDecimal minBaseTrade,
      BigDecimal usdValue) {
    this.id = id;
    this.name = name;
    this.symbol = symbol;
    this.status = status;
    this.statusMessage = statusMessage;
    this.listingStatus = listingStatus;
    this.minWithDraw = minWithDraw;
    this.withdrawFee = withdrawFee;
    this.minBaseTrade = minBaseTrade;
    this.usdValue = usdValue;
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
  
  @JsonGetter("usd")
  public BigDecimal inUSD() {
    return usdValue;
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).add("name", name).add("symbol", symbol)
        .add("listingStatus", listingStatus).add("usdValue", inUSD()).toString();
  }
}
