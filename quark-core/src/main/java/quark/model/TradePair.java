package quark.model;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;

import quark.CryptopiaCurrency;
import quark.CurrencyManager;

/**
 * <code> { "Id": 4909, "Label": "BTC/USDT", "Currency": "Bitcoin", "Symbol": "BTC", "BaseCurrency":
 * "Tether", "BaseSymbol": "USDT", "Status": "OK", "StatusMessage": null, "TradeFee": 0.2,
 * "MinimumTrade": 1.0E-8, "MaximumTrade": 1.0E8, "MinimumBaseTrade": 1.0, "MaximumBaseTrade":
 * 1.0E8, "MinimumPrice": 1.0E-8, "MaximumPrice": 1.0E8 }
 * 
 * <code>
 */
public class TradePair {
  private int id;
  private String label;
  private String symbol;
  private CryptopiaCurrency currency;
  private String baseSymbol;
  private CryptopiaCurrency baseCurrency;
  private BigDecimal tradeFee;
  private BigDecimal minimumTrade;
  private BigDecimal minimumBaseTrade;
  private String status;

  public TradePair(int id, String label, String symbol, CryptopiaCurrency currency,
      String baseSymbol, CryptopiaCurrency baseCurrency, BigDecimal tradeFee,
      BigDecimal minimumTrade, BigDecimal minimumBaseTrade, String status) {
    this.id = id;
    this.label = label;
    this.symbol = symbol;
    this.currency = currency;
    this.baseSymbol = baseSymbol;
    this.baseCurrency = baseCurrency;
    this.tradeFee = tradeFee;
    this.minimumTrade = minimumTrade;
    this.minimumBaseTrade = minimumBaseTrade;
    this.status = status;
  }

  public TradePair(JsonNode node, CurrencyManager currencyManager) throws ExecutionException {
    this.id = node.get("Id").asInt();
    this.symbol = node.get("Symbol").asText();
    this.label = node.get("Label").asText();
    this.baseSymbol = node.get("BaseSymbol").asText();
    this.currency = currencyManager.getCurrency(symbol).get();
    this.baseCurrency = currencyManager.getCurrency(baseSymbol).get();
    this.tradeFee = new BigDecimal(node.get("TradeFee").asText());
    this.minimumTrade = new BigDecimal(node.get("MinimumTrade").asText());
    this.minimumBaseTrade = new BigDecimal(node.get("MinimumBaseTrade").asText());
    this.status = node.get("Status").asText();
  }

  /**
   * the 2nd part of the pair the thing you are buying into
   * 
   * @return
   */
  public String getBaseSymbol() {
    return baseSymbol;
  }

  public CryptopiaCurrency getCurrency() {
    return currency;
  }

  /**
   * base is the market that alt coins can be bought in currency cc has btc,usdt,nzdt
   * 
   * @return
   * @throws ExecutionException
   */
  public CryptopiaCurrency getBaseCurrency() {
    return baseCurrency;
  }

  public BigDecimal getTradeFee() {
    return tradeFee;
  }

  /**
   * first part of the pair the thing you are sell into
   * 
   * @return
   */
  public String getSymbol() {
    return symbol;
  }

  public boolean isClosing() {
    if (status.equals("Closing")) {
      return true;
    }
    return false;
  }

  public String getLabel() {
    return label;
  }

  public int getId() {
    return id;
  }

  public BigDecimal getMinimumTrade() {
    return minimumTrade;
  }

  public BigDecimal getMinimumBaseTrade() {
    return minimumBaseTrade;
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", getId()).add("label", getLabel())
        .add("minTrade", getMinimumTrade()).add("minBaseTrade", getMinimumBaseTrade()).toString();
  }
}
