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
  private JsonNode node;
  private CurrencyManager currencyManager;

  public TradePair(JsonNode jsonNode, CurrencyManager currencyManager) {
    this.node = jsonNode;
    this.currencyManager = currencyManager;
  }

  /**
   * the 2nd part of the pair the thing you are buying into
   * 
   * @return
   */
  public String getBaseSymbol() {
    return node.get("BaseSymbol").asText();
  }

  public CryptopiaCurrency getCurrency() throws ExecutionException {
    return currencyManager.getCurrency(getSymbol()).get();
  }

  /**
   * base is the market that alt coins can be bought in currency cc has btc,usdt,nzdt
   * 
   * @return
   * @throws ExecutionException
   */
  public CryptopiaCurrency getBaseCurrency() throws ExecutionException {
    return currencyManager.getCurrency(getBaseSymbol()).get();
  }

  public BigDecimal getTradeFee() {
    return new BigDecimal(node.get("BaseSymbol").asText());
  }

  /**
   * first part of the pair the thing you are sell into
   * 
   * @return
   */
  public String getSymbol() {
    return node.get("Symbol").asText();
  }

  public boolean isClosing() {
    if (node.get("Status").asText().equals("Closing")) {
      return true;
    }
    return false;
  }

  public String getLabel() {
    return node.get("Label").asText();
  }

  public int getId() {
    return node.get("Id").asInt();
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
