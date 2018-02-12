package quark;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import quark.algorithms.Parameter;
import quark.algorithms.SimulationReport;
import quark.charts.PlotlyTrace;
import quark.db.DatabaseManager;
import quark.orders.ProcessedOrder;

@RestController
public class MarketSimulatorController {
  private CurrencyManager currencyManager;
  private DatabaseManager dbManager;

  private MarketManager marketManager;

  private Map<String, SimulationReport> runningSimulations = new ConcurrentHashMap<>();

  private ExecutorService executor = Executors.newSingleThreadExecutor();

  @Autowired
  public MarketSimulatorController(CurrencyManager currentManager, DatabaseManager dbManager,
      MarketManager marketManager) {
    this.currencyManager = currentManager;
    this.dbManager = dbManager;
    this.marketManager = marketManager;
  }

  @RequestMapping(path = "/api/simulate", method = RequestMethod.GET)
  public String simulate(@RequestParam(name = "tickRate", defaultValue = "P1D") String tickRate,
      @RequestParam(name = "shortAvg", defaultValue = "P1D") String shortAvg,
      @RequestParam(name = "longAvg", defaultValue = "P3D") String longAvg) throws Exception {
    String id = UUID.randomUUID().toString();
    SimulationReport report = new SimulationReport(id);
    report.getParams().put(Parameter.STARTING_FUND,"100");
    report.getParams().put(Parameter.TICK_RATE,tickRate);
    report.getParams().put(Parameter.SHORT_AVG,shortAvg);
    report.getParams().put(Parameter.LONG_AVG,longAvg);
    RunSimulation simulation = new RunSimulation(report, currencyManager, dbManager, marketManager);
    runningSimulations.put(id, report);
    executor.execute(simulation);
    return id;
  }

  @RequestMapping(path = "/api/simulations", method = RequestMethod.GET)
  public Set<String> getSimulations() {
    return runningSimulations.keySet();
  }

  @RequestMapping(path = "/api/simulation/{id}", method = RequestMethod.GET)
  public SimulationReport getSimulationReport(@PathVariable String id) {
    return runningSimulations.get(id);
  }

  @RequestMapping(path = "/api/simulation/{id}/plots", method = RequestMethod.GET)
  public Collection<PlotlyTrace> getPlots(@PathVariable String id) {
    return getSimulationReport(id).getPlots();
  }

  @RequestMapping(path = "/api/simulation/{id}/orders", method = RequestMethod.GET)
  public Set<ProcessedOrder> getProcessedOrders(@PathVariable String id) {
    return getSimulationReport(id).getProcessedOrders();
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
