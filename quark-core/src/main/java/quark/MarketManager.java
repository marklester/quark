package quark;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;

import quark.db.OrderDAO;
import quark.model.Market;
import quark.orders.Order.OrderType;

public class MarketManager {
  private static Logger LOGGER = LoggerFactory.getLogger(MarketManager.class);

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final String CACHE = "CACHE";

  private LoadingCache<String, Map<String, Market>> cache = CacheBuilder.newBuilder()
      .expireAfterWrite(10, TimeUnit.MINUTES).build(new CacheLoader<String, Map<String, Market>>() {
        public Map<String, Market> load(String key) throws Exception {
          return retrieveMarkets();
        }
      });

  private TradePairManager tpManager;

  public MarketManager(TradePairManager tpManager) {
    this.tpManager = tpManager;
  }

  private Map<String, Market> retrieveMarkets() throws Exception {
    String getMarketsUrl = CryptopiaGetter.BASE_CRYPTOPIA_API_URL + "GetMarkets";
    HttpClient client = HttpClientBuilder.create()
        .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();
    HttpGet get = new HttpGet(getMarketsUrl);
    HttpResponse response = client.execute(get);
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      JsonNode jsonNode = MAPPER.readTree(response.getEntity().getContent());

      if (jsonNode.get("Success").asBoolean() == false) {
        throw new ParseException("retrieval failure:" + jsonNode, null);
      }
      Map<String, Market> markets = Maps.newHashMap();
      for (JsonNode node : jsonNode.get("Data")) {
        try {
          Market market = new Market(node, tpManager);
          if (!market.getTradePair().isClosing() && market.getTradePair().getCurrency().inUSD() != null) {

            markets.put(market.getLabel(), market);
          }
        } catch (Exception e) {
          LOGGER.error("Could not create market for node: {}", node);
        }
      }
      return markets;
    }
    throw new ParseException("Cound not parse json " + response.getStatusLine(), null);
  }

  public Market getMarket(String label) throws ExecutionException {
    return cache.get(CACHE).get(label);
  }

  public Collection<Market> getMarkets() throws ExecutionException {
    return cache.get(CACHE).values();
  }

  public Collection<Market> getMarkets(double minVolume) throws ExecutionException {
    return cache.get(CACHE).values().stream().filter(m -> m.getVolume() > minVolume).collect(Collectors.toSet());
  }

  public Collection<Market> getMarketsWithMinOrders(OrderDAO orderDao, double orders) throws ExecutionException {
    return cache.get(CACHE).values().stream()
        .filter(m -> orderDao.getOrderCount(m.getTradePair().getId(), OrderType.ALL) >= orders)
        .collect(Collectors.toSet());
  }

  Market updateMarket(String label, TradePairManager tradePairs) throws Exception {
    HttpClient client = HttpClientBuilder.create().build();
    HttpGet get = new HttpGet(CryptopiaGetter.BASE_CRYPTOPIA_API_URL + "GetMarket/" + label);
    HttpResponse response = client.execute(get);
    JsonNode jsonNode = MAPPER.readTree(EntityUtils.toByteArray(response.getEntity()));
    return new Market(jsonNode.get("Data"), tradePairs);
  }

  public TradePairManager getTradePairManager() {
    return tpManager;
  }
}
