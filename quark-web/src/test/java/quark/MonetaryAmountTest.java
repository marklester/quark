package quark;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import quark.model.CurrencyLookup;
import quark.model.MonetaryAmount;

public class MonetaryAmountTest {
  ObjectMapper mapper = new ObjectMapper();
  private CurrencyLookup lookup;
  
  @Before
  public void setup() throws Exception{
    lookup = CurrencyLookup.from(new File("src/test/resources/coinmarketcap_currencies.json"));
  }
  
  @Test
  public void testCreateLookupFromJSON() throws Exception {
    MonetaryAmount amount = lookup.bySymbol("GBX");
    Assert.assertNotNull(amount);
    Assert.assertEquals("GBX", amount.getSymbol());
    Assert.assertEquals(new BigDecimal("17.6661"), amount.getValue());
  }
  
  @Test
  public void testMoneyMath() throws ParseException{
    MonetaryAmount btc = lookup.bySymbol("BTC");
    System.out.println(btc);
    BigDecimal startingFund =
        new BigDecimal(100).divide(btc.getValue(), 10, RoundingMode.HALF_EVEN);
    System.out.println(startingFund);
  }
}
