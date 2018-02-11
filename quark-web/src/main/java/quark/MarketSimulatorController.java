package quark;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import quark.charts.PlotlyTrace;
import quark.db.DatabaseManager;

@RestController
public class MarketSimulatorController {
  private static Logger LOGGER = LoggerFactory.getLogger(MarketSimulatorController.class);

  private CurrencyManager currencyManager;
  private DatabaseManager dbManager;

  private MarketManager marketManager;

  private Map<String, CompletableFuture<Set<PlotlyTrace>>> runningSimulations =
      new ConcurrentHashMap<>();

  private ExecutorService executor = Executors.newSingleThreadExecutor();

  @Autowired
  public MarketSimulatorController(CurrencyManager currentManager, DatabaseManager dbManager,
      MarketManager marketManager) {
    this.currencyManager = currentManager;
    this.dbManager = dbManager;
    this.marketManager = marketManager;
  }

  @RequestMapping(path="/api/simulate", method = RequestMethod.GET)
  public String simulate() throws Exception {
    RunSimulation simulation = new RunSimulation(currencyManager, dbManager, marketManager);
    CompletableFuture<Set<PlotlyTrace>> task = asFuture(simulation, executor);
    String id = UUID.randomUUID().toString();
    runningSimulations.put(id, task);
    return id;
  }
  
  @RequestMapping(path="/api/simulations", method = RequestMethod.GET)
  public Set<String> getResults(){
    return runningSimulations.keySet();
  }
  
  @RequestMapping(path="/api/simulation/{id}", method = RequestMethod.GET)
  public Set<PlotlyTrace> getResults(@PathVariable String id){
    CompletableFuture<Set<PlotlyTrace>> ft = runningSimulations.get(id);
    if(ft!=null) {
      return ft.getNow(Collections.emptySet());      
    }
    return Collections.emptySet();
  }

  public static <T> CompletableFuture<T> asFuture(Callable<? extends T> callable,
      Executor executor) {
    CompletableFuture<T> future = new CompletableFuture<>();
    executor.execute(() -> {
      try {
        future.complete(callable.call());
      } catch (Throwable t) {
        future.completeExceptionally(t);
      }
    });
    return future;
  }
}
