package quark.mining;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import quark.model.CoinMarketCapMoney;
import quark.model.Currencies;
import quark.model.Currency;
import quark.model.MonetaryAmount;
import quark.model.ParseException;
import quark.model.StandardMoney;
import quark.model.WalletId;

public class MiningMonitor {
  private static final Logger LOGGER = LogManager.getLogger(MiningMonitor.class);

  private static final String MINING_URL = "https://www.unimining.net/api/wallet?address=";
  private ObjectMapper mapper = new ObjectMapper();

  private List<WalletId> wallets;

  public MiningMonitor(List<WalletId> wallets) {
    this.wallets = wallets;
  }

  MiningStatus getStatus(WalletId wallet) throws Exception {
    String miningWalletUrl = MINING_URL + wallet.getAddress();
    HttpClient client = HttpClientBuilder.create().build();
    HttpGet get = new HttpGet(miningWalletUrl);
    HttpResponse response = client.execute(get);
    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
      System.out.printf("error getting status:%s\n", response.getStatusLine());
      LOGGER.error("error getting status:{}", response.getStatusLine());
      return null;
    }
    JsonNode node = mapper.readTree(response.getEntity().getContent());
    return new MiningStatus(node, wallet.getCurrency());
  }

  public void status() throws Exception {
    for (WalletId wallet : wallets) {
      System.out.println(getStatus(wallet));
    }
  }

  public static void main(String[] args) throws Exception {
    List<WalletId> wallets = Lists.newArrayList(
        new WalletId(Currencies.GOBYTE, "", "GVTZSgB6kwjKnPiX6yfXGBSG6jzLjeLbo2"),
        new WalletId(Currencies.TREZARCOIN, "", "TmmZcgJiEJ5nX3vUshVdJdzWJ8nC7EBciK"));
    MiningMonitor monitor = new MiningMonitor(wallets);
    ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
    service.scheduleAtFixedRate(() -> {
      try {
        monitor.status();
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }, 0, 5, TimeUnit.MINUTES);
    monitor.status();
    service.awaitTermination(100, TimeUnit.HOURS);
  }
}


class MiningStatus {
  private JsonNode node;
  private MonetaryAmount currentPrice;
  private Currency currency;

  MiningStatus(JsonNode node, Currency currency) throws ParseException {
    this.node = node;
    this.currency = currency;
    this.currentPrice = CoinMarketCapMoney.create(currency.getName());
  }

  MonetaryAmount getNextPayment() {
    BigDecimal balance = new BigDecimal(node.get("balance").asText());
    return new StandardMoney(balance, currency.getSymbol());
  }

  MonetaryAmount getUnPaid() {
    BigDecimal balance = new BigDecimal(node.get("unpaid").asText());
    return new StandardMoney(balance, currency.getSymbol());
  }

  BigDecimal inUsd(MonetaryAmount amt) {
    return amt.getAmount().multiply(currentPrice.getAmount());
  }

  public String toString() {
    MonetaryAmount nextPayment = getNextPayment();
    MonetaryAmount balance = getUnPaid();
    return String.format("%s-%s: nextPayment:%s($%s) balance:%s($%s)", Instant.now(),currency.getSymbol(),
        nextPayment.getAmount(), inUsd(nextPayment), balance.getAmount(), inUsd(balance));
  }
}
