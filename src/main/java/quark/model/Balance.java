package quark.model;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import quark.CryptopiaCurrency;
import quark.CurrencyManager;
import quark.ParseException;

/**
 * { "Success":true, "Error":null, "Data":[ { "CurrencyId":1, "Symbol":"BTC", "Total":"10300",
 * "Available":"6700.00000000", "Unconfirmed":"2.00000000", "HeldForTrades":"3400,00000000",
 * "PendingWithdraw":"200.00000000", "Address":"4HMjBARzTNdUpXCYkZDTHq8vmJQkdxXyFg", "BaseAddress":
 * "ZDTHq8vmJQkdxXyFgZDTHq8vmJQkdxXyFgZDTHq8vmJQkdxXyFg", "Status":"OK", "StatusMessage":"" }, { ...
 * } ] }
 **/
public class Balance {
  private final CryptopiaCurrency currency;
  private final int currencyId;
  private final String symbol;
  private final BigDecimal available;

  public Balance(CryptopiaCurrency currency, BigDecimal available) {
    Preconditions.checkNotNull(currency, "Currency can't be null");
    this.currency = currency;
    this.currencyId = currency.getId();
    this.symbol = currency.getSymbol();
    this.available = available;
  }

  public Balance(JsonNode currencyNode, CurrencyManager currencyManager) throws ExecutionException {
    this.currencyId = currencyNode.get("CurrencyId").asInt();
    this.currency = currencyManager.getCurrency(currencyId);
    this.symbol = currencyNode.get("Symbol").asText();
    this.available = new BigDecimal(currencyNode.get("Available").asText());

  }

  public int getCurrencyId() {
    return currencyId;
  }

  public String getSymbol() {
    return symbol;
  }

  public CryptopiaCurrency getCurrency() {
    return currency;
  }

  public BigDecimal getAvailable() {
    return available;
  }

  public Balance substract(BigDecimal amountOfChange) {
    BigDecimal newAmount = available.subtract(amountOfChange);
    return new Balance(getCurrency(), newAmount);
  }

  public Balance add(BigDecimal amountOfChange) {
    BigDecimal newAmount = available.add(amountOfChange);
    return new Balance(getCurrency(), newAmount);
  }
  
  public MonetaryAmount toUSD() throws ParseException {
      return CoinMarketCapMoney.create(currency.getName().toLowerCase());
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("currency", getCurrency().getName())
        .add("available", getAvailable()).toString();
  }
}
