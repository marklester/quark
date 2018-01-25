package quark;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import quark.algorithms.Algorithm;
import quark.model.Balance;
import quark.model.Balances;
import quark.model.OpenOrder;

public class Trader {
  public static final String BASE_CRYPTOPIA_API_URL = "https://www.cryptopia.co.nz/Api/";
  private static ObjectMapper mapper = new ObjectMapper();
  private String apiKey = "1e6e97ff5b3f436ebe876ef321ee811a";
  private String privateKey = "sUC+JejXPucVPXpmNcfFGAxflKmMa97pidhYD2ksvro=";

  HttpClient client = HttpClientBuilder.create().build();
  private List<Algorithm> algorithms;

  List<Balance> getBalance() throws Exception {
    String urlMethod = BASE_CRYPTOPIA_API_URL + "GetBalance";


    URLConnection con = new URL(urlMethod).openConnection(); // CREATE POST CONNECTION
    con.setDoOutput(true);

    HttpsURLConnection httpsConn = (HttpsURLConnection) con;
    httpsConn.setRequestMethod("POST");
    httpsConn.setInstanceFollowRedirects(true);
    String postParam = "{}";
    con.setRequestProperty("Authorization", generateAuth(urlMethod, postParam));
    con.setRequestProperty("Content-Type", "application/json");

    // WRITE POST PARAMS
    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
    wr.writeBytes(postParam);
    wr.flush();
    wr.close();

    // READ THE RESPONSE

    String inputLine;
    StringBuilder response = new StringBuilder();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
    }

    JsonNode node = mapper.readTree(response.toString());
    return Balances.convertToBalance(node);
  }
  
  void addAlgorithm(Algorithm algorithm){
    algorithms.add(algorithm);
  }
  
  void applyAlgorithms(){
    
  }
  
  String generateAuth(String urlMethod, String postParams) throws Exception {
    String nonce = String.valueOf(System.currentTimeMillis());
    String reqSignature = apiKey + "POST"
        + URLEncoder.encode(urlMethod, StandardCharsets.UTF_8.toString()).toLowerCase() + nonce
        + getMD5_B64(postParams);

    return "amx " + apiKey + ":" + sha256_B64(reqSignature) + ":" + nonce;
  }

  private String getMD5_B64(String postParameter) throws Exception {
    return Base64.getEncoder()
        .encodeToString(MessageDigest.getInstance("MD5").digest(postParameter.getBytes("UTF-8")));
  }

  private String sha256_B64(String msg) throws Exception {
    Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
    SecretKeySpec secret_key =
        new SecretKeySpec(Base64.getDecoder().decode(privateKey), "HmacSHA256");
    sha256_HMAC.init(secret_key);
    return Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(msg.getBytes("UTF-8")));
  }

  public MarketManager getMarketManager() throws Exception {
    return MarketManager.create(getTradePairManager());
  }
  
  public long getAvg(long tradePairId, Duration overTime){
    return 0;
  }
  
  public TradePairManager getTradePairManager() throws Exception {
    return TradePairManager.create();
  }

  Set<OpenOrder> getOpenOrders(){
    return null;
  }
  
  Set<OpenOrder> getOpenOrders(long tpId){
    return null;
  }
  
  public void start() throws Exception {
    DatabaseManager dbManager = new DatabaseManager();
    dbManager.start();
    dbManager.createTables();
    MarketHistory mhistory = new MarketHistory(dbManager, getMarketManager());
    mhistory.startPolling();
  }
  
  /**
   * 
   * @param tpId the coin to buy
   * @param the percent of balance to use
   */
  public void order(long tpId, double d) {
    // TODO Auto-generated method stub
    
  }
}
