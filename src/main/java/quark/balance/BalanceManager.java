package quark.balance;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;

import quark.CryptopiaGetter;
import quark.CurrencyManager;
import quark.model.Balance;

public class BalanceManager {
  private static final String CACHE = "CACHE";

  private LoadingCache<String, Map<Integer, Balance>> cache =
      CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
          .build(new CacheLoader<String, Map<Integer, Balance>>() {
            public Map<Integer, Balance> load(String key) throws Exception {
              return retrieveBalances();
            }
          });

  private CurrencyManager currencyManager;

  public BalanceManager(CurrencyManager currencyManager) {
    this.currencyManager = currencyManager;
  }

  public Balance getBalance(int currencyID) {
    return cache.getIfPresent(CACHE).get(currencyID);
  }

  public Collection<Balance> getBalances() {
    return cache.getIfPresent(CACHE).values();
  }

  private Map<Integer, Balance> retrieveBalances() throws Exception {
    String urlMethod = CryptopiaGetter.BASE_CRYPTOPIA_API_URL + "GetBalance";
    CryptopiaGetter getter = new CryptopiaGetter();

    JsonNode node = getter.get(urlMethod);
    JsonNode data = node.get("Data");
    Map<Integer, Balance> balances = Maps.newHashMap();
    for (JsonNode balanceNode : data) {
      Balance balance = new Balance(balanceNode, currencyManager);
      balances.put(balance.getCurrencyId(), balance);
    }
    return balances;
  }
}
