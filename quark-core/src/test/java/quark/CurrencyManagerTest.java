package quark;

import java.io.File;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import quark.model.CurrencyLookup;

public class CurrencyManagerTest {
  @Test
  public void testGetCurrencies() throws Exception{
    CurrencyLookup lookup = CurrencyLookup.from(new File("src/test/resources/coinmarketcap_currencies.json"));
    
    CurrencyManager cman = new CurrencyManager(lookup);
    Collection<CryptopiaCurrency> currencies = cman.getCurrencies();
    Assert.assertTrue(currencies.size()>0);
    currencies.forEach(c->System.out.println(c));
  }
}
