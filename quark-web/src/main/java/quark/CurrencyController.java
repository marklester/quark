package quark;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import quark.model.Currency;
import quark.model.MonetaryAmount;

@RestController
public class CurrencyController {
  private CurrencyManager currencyManager;

  @Autowired
  public CurrencyController(CurrencyManager currencyManager) {
    this.currencyManager = currencyManager;
  }
  
  @RequestMapping(path="/api/currencies",method=RequestMethod.GET)
  public Collection<Currency> getCurrencies() throws ExecutionException{
    return new HashSet<Currency>(currencyManager.getCurrencies());
  }
  
  @RequestMapping(path="/api/currency/{symbol}/usd",method=RequestMethod.GET)
  public MonetaryAmount getCurrencyUSD(@PathVariable String symbol) throws ExecutionException{
    return currencyManager.getCurrencyLookup().bySymbol(symbol);
  }
  
  @RequestMapping(path="/api/currency/{symbol}",method=RequestMethod.GET)
  public CryptopiaCurrency getCurrency(@PathVariable String symbol) throws ExecutionException{
    return currencyManager.getCurrency(symbol).get();
  }
}
