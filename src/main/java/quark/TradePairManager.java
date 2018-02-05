package quark;

import java.util.Collection;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

import quark.model.TradePair;

public class TradePairManager {
  private static Logger LOGGER = LoggerFactory.getLogger(TradePairManager.class);
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static TradePairManager create(CurrencyManager currencyManager) throws Exception {
    HttpClient client = HttpClientBuilder.create().build();
    String tradePairUrl = CryptopiaGetter.BASE_CRYPTOPIA_API_URL + "GetTradePairs/";
    HttpGet get = new HttpGet(tradePairUrl);
    HttpResponse response = client.execute(get);

    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
      throw new ParseException("could not create trade manager " + response.getStatusLine());
    }

    JsonNode jsonNode = MAPPER.readTree(response.getEntity().getContent());
    return new TradePairManager(jsonNode.get("Data"), currencyManager);
  }

  private Map<Integer, TradePair> tradePairs = Maps.newHashMap();

  public TradePairManager(JsonNode tradePairsJson, CurrencyManager currencyManager) {
    this.tradePairs = Maps.newHashMap();
    for (JsonNode node : tradePairsJson) {
      try {
        TradePair tp = new TradePair(node, currencyManager);
        tradePairs.put(tp.getId(), tp);
      } catch (Exception e) {
        LOGGER.error("could not get trade pair for node {} ",node, e);
      }
    }
  }

  public TradePair getTradePair(int id) {
    return tradePairs.get(id);
  }

  public Collection<TradePair> getTradePairs() {
    return tradePairs.values();
  }
}
