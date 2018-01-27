package quark;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CurrencyManager {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String CACHE = "CACHE";
  
  private LoadingCache<String, Map<Integer, CryptopiaCurrency>> graphs =
      CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
          .build(new CacheLoader<String, Map<Integer, CryptopiaCurrency>>() {
            public Map<Integer, CryptopiaCurrency> load(String key) throws Exception {
              return retrieveCurrencies();
            }
          });

  Map<Integer, CryptopiaCurrency> retrieveCurrencies() throws Exception {
    String currenciesURL = CryptopiaGetter.BASE_CRYPTOPIA_API_URL + "GetCurrencies";
    HttpClient client = HttpClientBuilder.create()
        .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
        .build();
    HttpGet get = new HttpGet(currenciesURL);
    HttpResponse response = client.execute(get);
    JsonNode jsonNode = MAPPER.readTree(response.getEntity().getContent());
    return StreamSupport.stream(jsonNode.get("Data").spliterator(), false)
        .map(node -> new CryptopiaCurrency(node)).collect(Collectors.toMap(k -> k.getId(), v -> v));
  }

  public CryptopiaCurrency getCurrency(int currencyId){
    return graphs.getIfPresent(CACHE).get(currencyId);
  }
  
  public Collection<CryptopiaCurrency> getCurrencies(){
    return graphs.getIfPresent(CACHE).values();
  }
}
