package quark;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

public class TradePairManager {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static TradePairManager create() throws Exception {
    HttpClient client = HttpClientBuilder.create().build();
    String tradePairUrl = Trader.BASE_CRYPTOPIA_API_URL + "GetTradePairs/";
    HttpGet get = new HttpGet(tradePairUrl);
    HttpResponse response = client.execute(get);

    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
      throw new ParseException("could not create trade manager " + response.getStatusLine());
    }

    JsonNode jsonNode = MAPPER.readTree(response.getEntity().getContent());
    return new TradePairManager(jsonNode.get("Data"));
  }

  private Map<Long, TradePair> tradePairs = Maps.newHashMap();

  public TradePairManager(JsonNode tradePairsJson) {
    tradePairs =
        StreamSupport.stream(tradePairsJson.spliterator(), false).map(node -> new TradePair(node))
            .collect(Collectors.toMap(tpair -> tpair.getId(), tpair -> tpair));
  }

  public TradePair getTradePair(long id) {
    return tradePairs.get(id);
  }

  public Collection<TradePair> getTradePairs() {
    return tradePairs.values();
  }
}
