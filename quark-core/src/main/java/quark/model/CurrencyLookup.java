package quark.model;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

import quark.ParseException;

/**
 * [ { "id": "bitcoin", "name": "Bitcoin", "symbol": "BTC", "rank": "1", "price_usd": "6692.5",
 * "price_btc": "1.0", "24h_volume_usd": "9150870000.0", "market_cap_usd": "112758331935",
 * "available_supply": "16848462.0", "total_supply": "16848462.0", "max_supply": "21000000.0",
 * "percent_change_1h": "-4.81", "percent_change_24h": "-18.02", "percent_change_7d": "-40.19",
 * "last_updated": "1517881169" }]
 *
 */
public class CurrencyLookup {
  private static final String COIN_MARKET_CAP_URL = "https://api.coinmarketcap.com/v1/ticker/";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static CurrencyLookup create() throws ParseException {
    try {
      HttpResponse response =
          Request.Get(new URIBuilder(COIN_MARKET_CAP_URL).addParameter("limit", "0").build()).execute()
              .returnResponse();
      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        throw new HttpException("could not retrieve info " + response.getStatusLine());
      }
      JsonNode jsonNode = MAPPER.readTree(response.getEntity().getContent());
      Map<String, MonetaryAmount> currencies = Maps.newHashMap();
      for (JsonNode node : jsonNode) {
        BigDecimal amount = new BigDecimal(node.get("price_usd").asText("0"));
        StandardMoney money = new StandardMoney(amount, node.get("symbol").asText());
        currencies.put(money.getSymbol(), money);
      }
      return new CurrencyLookup(currencies);
    } catch (IOException | HttpException | URISyntaxException ex) {
      throw new ParseException("error creating CurrencyLookup from coinmarketcap", ex);
    }
  }
  
  public static CurrencyLookup from(File file) throws Exception {
    JsonNode jsonNode = MAPPER.readTree(file);
    Map<String, MonetaryAmount> currencies = Maps.newHashMap();
    for (JsonNode node : jsonNode) {
      BigDecimal amount = new BigDecimal(node.get("price_usd").asText("0"));
      StandardMoney money = new StandardMoney(amount, node.get("symbol").asText());
      currencies.put(money.getSymbol(), money);
    }
    return new CurrencyLookup(currencies);
  }

  private Map<String, MonetaryAmount> currencies = Maps.newHashMap();

  public CurrencyLookup(Map<String, MonetaryAmount> currencies) {
    this.currencies = currencies;
  }

  public MonetaryAmount bySymbol(String symbol) {
    return currencies.getOrDefault(symbol.toUpperCase(), new StandardMoney(null, symbol));
  }

  public Collection<MonetaryAmount> getCurrencies() {
    return currencies.values();
  }


}
