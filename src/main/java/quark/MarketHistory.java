package quark;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import quark.model.CoinMarketCapMoney;
import quark.model.MonetaryAmount;

public class MarketHistory {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static List<Order> createMarketHistory(long id) throws Exception {
    String getHistoryUrl = Trader.BASE_CRYPTOPIA_API_URL + "GetMarketHistory/" + id + "/48";
    HttpClient client = HttpClientBuilder.create().build();
    HttpGet get = new HttpGet(getHistoryUrl);
    HttpResponse response = client.execute(get);
    JsonNode jsonNode = MAPPER.readTree(response.getEntity().getContent());
    List<Order> orders = StreamSupport.stream(jsonNode.get("Data").spliterator(), false)
        .map(node -> new Order(node)).collect(Collectors.toList());
    return orders;
  }

  public static void main(String args[]) throws Exception {
    List<Order> orders = createMarketHistory(5719);
    int count = 0;
    for (Order order : orders) {
      System.out.printf("%s: %s\n", count, order);
      count += 1;
    }
    Comparator<Order> comp = (o1, o2) -> o1.getPrice().compareTo(o2.getPrice());
    Order max = orders.stream().max(comp).get();
    Order min = orders.stream().min(comp).get();
    MonetaryAmount btc = CoinMarketCapMoney.create("bitcoin");
    System.out.printf("High: %s %s\n Low:%s %s\n", btc.getAmount().multiply(max.getPrice()), max,
        btc.getAmount().multiply(min.getPrice()), min);
  }
}
