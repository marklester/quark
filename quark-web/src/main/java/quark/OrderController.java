package quark;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import quark.db.DatabaseManager;

@RestController
public class OrderController {
  private DatabaseManager dbManager;

  @Autowired
  public OrderController(DatabaseManager dbManager) {
    this.dbManager = dbManager;
  }
  
  @RequestMapping(path="/api/orders/size",method=RequestMethod.GET)
  public Map<String, Integer> getTotalOrders(@RequestParam(name="grouping",required=false) String grouping){
    if(grouping!=null) {
      return dbManager.getOrderDao().countOrdersBy(grouping);
    }
    return ImmutableMap.of("ALL", dbManager.getOrderDao().getOrderCount());
  }
}
