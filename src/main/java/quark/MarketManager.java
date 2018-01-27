package quark;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

import quark.model.Market;
import quark.model.ParseException;

public class MarketManager {
  private static Logger LOGGER = LoggerFactory.getLogger(MarketHistory.class);

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static MarketManager create(TradePairManager tradePairManager) throws Exception {
    String getMarketsUrl = CryptopiaGetter.BASE_CRYPTOPIA_API_URL + "GetMarkets";
    HttpClient client = HttpClientBuilder.create()
        .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
        .build();
    HttpGet get = new HttpGet(getMarketsUrl);
    HttpResponse response = client.execute(get);
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      JsonNode jsonNode = MAPPER.readTree(response.getEntity().getContent());
      
      if(jsonNode.get("Success").asBoolean()==false) {
        throw new ParseException("retreival failure:"+jsonNode,null);
      }
      
      List<Market> markets = StreamSupport.stream(jsonNode.get("Data").spliterator(), false)
          .map(marketNode -> new Market(marketNode, tradePairManager)).collect(Collectors.toList());

      return new MarketManager(markets);
    }
    throw new quark.model.ParseException("Cound not parse json " + response.getStatusLine(), null);
  }

  private Map<String, Market> markets = Maps.newHashMap();

  public MarketManager(List<Market> markets) {
    this.markets = markets.stream().filter(market -> !market.getTradePair().isClosing())
        .collect(Collectors.toMap(k -> k.getLabel(), v -> v));
  }

  public Market getMarket(String label) {
    return markets.get(label);
  }

  public Collection<Market> getMarkets() {
    return markets.values();
  }

  public Market updateMarket(String label, TradePairManager tradePairs) throws Exception {
    HttpClient client = HttpClientBuilder.create().build();
    HttpGet get = new HttpGet(CryptopiaGetter.BASE_CRYPTOPIA_API_URL + "GetMarket/" + label);
    HttpResponse response = client.execute(get);
    JsonNode jsonNode = MAPPER.readTree(EntityUtils.toByteArray(response.getEntity()));
    return new Market(jsonNode.get("Data"), tradePairs);
  }
}
