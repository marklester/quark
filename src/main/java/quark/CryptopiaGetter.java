package quark;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CryptopiaGetter {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  public static final String BASE_CRYPTOPIA_API_URL = "https://www.cryptopia.co.nz/Api/";
  private String apiKey = "1e6e97ff5b3f436ebe876ef321ee811a";
  private String privateKey = "sUC+JejXPucVPXpmNcfFGAxflKmMa97pidhYD2ksvro=";
  
  public JsonNode get(String url) throws Exception{
    URLConnection con = new URL(url).openConnection(); // CREATE POST CONNECTION
    con.setDoOutput(true);

    HttpsURLConnection httpsConn = (HttpsURLConnection) con;
    httpsConn.setRequestMethod("POST");
    httpsConn.setInstanceFollowRedirects(true);
    String postParam = "{}";
    con.setRequestProperty("Authorization", generateAuth(url, postParam));
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

    return MAPPER.readTree(response.toString());
  }
  
  private String generateAuth(String urlMethod, String postParams) throws Exception {
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
}
