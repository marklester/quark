package quark.model;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.JsonNode;

import quark.CryptopiaCurrency;
import quark.CurrencyManager;

/**
{
  "Success":true,
  "Error":null,
  "Data":[
          {
              "CurrencyId":1,
              "Symbol":"BTC",
              "Total":"10300",
              "Available":"6700.00000000",
              "Unconfirmed":"2.00000000",
              "HeldForTrades":"3400,00000000",
              "PendingWithdraw":"200.00000000",
              "Address":"4HMjBARzTNdUpXCYkZDTHq8vmJQkdxXyFg",
      "BaseAddress": "ZDTHq8vmJQkdxXyFgZDTHq8vmJQkdxXyFgZDTHq8vmJQkdxXyFg",
              "Status":"OK",
              "StatusMessage":""
          },
          {
              ...
          }
         ]
}**/
public class Balance {
  CryptopiaCurrency currency;

  private JsonNode currencyNode;

  private CurrencyManager currencyManager;

  public Balance(JsonNode currencyNode, CurrencyManager currencyManager) {
    this.currencyNode = currencyNode;
    this.currencyManager = currencyManager;
  }

  public int getCurrencyId() {
    return currencyNode.get("CurrencyId").asInt();
  }

  public String getSymbol() {
    return currencyNode.get("Symbol").asText();
  }

  public CryptopiaCurrency getCurrency() throws ExecutionException {
    return currencyManager.getCurrency(getCurrencyId());
  }

  public BigDecimal getAvailable() {
    return new BigDecimal(currencyNode.get("Available").asText());
  }

  public String toString() {
    return currencyNode.toString();
  }
}
