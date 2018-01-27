package quark;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

public class CurrencyManagerTest {
  @Test
  public void testGetCurrencies() throws Exception{
    CurrencyManager cman = new CurrencyManager();
    Collection<CryptopiaCurrency> currencies = cman.getCurrencies();
    Assert.assertTrue(currencies.size()>0);
    currencies.forEach(c->System.out.println(c));
  }
}
