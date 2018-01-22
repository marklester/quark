package quark;

import java.io.File;
import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import quark.model.CoinMarketCapMoney;
import quark.model.MonetaryAmount;
import quark.model.ParseException;

public class MonetaryAmountTest {
  ObjectMapper mapper = new ObjectMapper();
  @Test
  public void testCreateMarketCapMoney() throws Exception {
    JsonNode priceJson = mapper.readTree(new File("src/test/resources/price.json"));
    MonetaryAmount amount = new CoinMarketCapMoney(priceJson);
    Assert.assertEquals("GBX", amount.getUnit());
    Assert.assertEquals(new BigDecimal("39.2322"), amount.getAmount());
  }
  
  @Test
  public void testCreateMarketCapMoneyFunctional() throws ParseException{
    MonetaryAmount goByte = CoinMarketCapMoney.create("gobyte"); 
    System.out.println(goByte);
    System.out.println(CoinMarketCapMoney.create("trezarcoin"));
  }
}
