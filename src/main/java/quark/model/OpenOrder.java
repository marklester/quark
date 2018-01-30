package quark.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
{
  "Success":true,
  "Error":null,
  "Data":[
           {
             "OrderId": 23467,
             "TradePairId": 100,
             "Market": "DOT/BTC",
             "Type": "Buy",
             "Rate": 0.00000034,
             "Amount": 145.98000000,
             "Total": "0.00004963",
             "Remaining": "23.98760000",
             "TimeStamp":"2014-12-07T20:04:05.3947572"
           },
           {
             ...........
           }
        ]
}**/
public class OpenOrder {
  long orderId;
  int tradePairId;
  String market;
  String type;
  BigDecimal rate;
  BigDecimal amount;
  BigDecimal total;
  BigDecimal remaining;
  LocalDate timestamp;
}
