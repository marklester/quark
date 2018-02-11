package quark;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import quark.db.DatabaseManager;
import quark.db.PostgresDatabaseManager;
import quark.model.CurrencyLookup;

@SpringBootApplication
public class QuarkServer {
  @Bean
  CurrencyLookup currencyLookup() throws ParseException {
    return CurrencyLookup.create();
  }

  @Bean
  CurrencyManager currentManager(CurrencyLookup currencyLookup) {
    return new CurrencyManager(currencyLookup);
  }

  @Bean
  TradePairManager tradePairManager(CurrencyManager currencyManager) throws Exception {
    return TradePairManager.create(currencyManager);
  }

  @Bean
  DatabaseManager dbManager() throws Exception {
    return new PostgresDatabaseManager();
  }

  @Bean
  ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    JavaTimeModule javaTimeModule = new JavaTimeModule();
    // Hack time module to allow 'Z' at the end of string (i.e. javascript json's)
    javaTimeModule.addDeserializer(LocalDateTime.class,
        new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));
    mapper.registerModule(javaTimeModule);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    return mapper;
  }

  @Bean
  MarketManager marketManager(TradePairManager tradePairManager) {
    return new MarketManager(tradePairManager);
  }

  public static void main(String[] args) throws Exception {
    // Trader realTrader =
    // new CryptopiaTrader(dbManager, currencyManager, fullMarketHistory, marketManager);
    // MarketHistory testHistory = new MarketHistory(inMemManager, marketManager);
    SpringApplication.run(QuarkServer.class, args);
  }
}
