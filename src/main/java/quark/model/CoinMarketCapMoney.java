package quark.model;

import java.io.IOException;
import java.math.BigDecimal;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;

/**
 * Money derived from CoinMarketCap * { "id": "gobyte", "name": "GoByte", "symbol": "GBX", "rank":
 * "391", "price_usd": "39.2322", "price_btc": "0.00325576", "24h_volume_usd": "253317.0",
 * "market_cap_usd": "21860518.0", "available_supply": "557209.0", "total_supply": "1382196.0",
 * "max_supply": "31800000.0", "percent_change_1h": "1.32", "percent_change_24h": "-18.27",
 * "percent_change_7d": "-42.59", "last_updated": "1516535063" }
 */
public class CoinMarketCapMoney implements MonetaryAmount {
  private static final Logger LOGGER = LogManager.getLogger(CoinMarketCapMoney.class);

  private static final String coinCapUrl = "https://api.coinmarketcap.com/v1/ticker/";
  private static final ObjectMapper mapper = new ObjectMapper();
  private JsonNode node;

  public static MonetaryAmount create(String currencyName) throws ParseException {
    String currencyUrl = coinCapUrl + currencyName;

    try {
      HttpClient client = HttpClientBuilder.create().build();
      HttpGet get = new HttpGet(currencyUrl);
      HttpResponse response = client.execute(get);
      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        throw new HttpException("could not retrieve info:" + response.getStatusLine());
      }
      JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("jsonMessage:{}", jsonNode);
      }

      JsonNode price = jsonNode.get(0);
      return new CoinMarketCapMoney(price);
    } catch (IOException | HttpException ex) {
      throw new ParseException("error creating MonetaryAmount from coinmarketcap", ex);
    }
  }

  public CoinMarketCapMoney(JsonNode node) {
    this.node = node;
  }

  public String getUnit() {
    return node.get("symbol").asText();
  }

  public BigDecimal getAmount() {
    return new BigDecimal(node.get("price_usd").asText());
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("amount", getAmount()).add("unit", getUnit())
        .toString();
  }
}
