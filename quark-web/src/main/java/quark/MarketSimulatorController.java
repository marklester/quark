package quark;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import quark.charts.PlotlyTrace;
import quark.db.DatabaseManager;
import quark.model.Balance;
import quark.orders.ProcessedOrder;
import quark.report.LapReport;
import quark.report.SimParams;
import quark.report.SimulationReport;

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

  @RequestMapping(path = "/api/simulate", method = RequestMethod.POST)
  public String simulate(@RequestBody SimParams params) throws Exception {
    String id = "" + (runningSimulations.size() + 1);
    SimulationReport report = new SimulationReport(id, params);
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
  public Set<ProcessedOrder> getProcessedOrders(@PathVariable String id,
      @RequestParam(name = "success", defaultValue = "true", required = false) Boolean success) {
    if (success != null) {
      return getSimulationReport(id).getProcessedOrders().stream()
          .filter(order -> order.isSuccess() == success).collect(Collectors.toSet());
    }
    return getSimulationReport(id).getProcessedOrders();
  }

  @RequestMapping(path = "/api/simulation/{id}/parameters", method = RequestMethod.GET)
  public SimParams getParameters(@PathVariable String id) {
    return getSimulationReport(id).getParams();
  }

  @RequestMapping(path = "/api/simulation/{id}/start-balances", method = RequestMethod.GET)
  public Set<Balance> getStartingBalances(@PathVariable String id) {
    return getSimulationReport(id).getParams().getStartingBalances().entrySet().stream()
        .map(entry -> new Balance(currencyManager.getCurrency(entry.getKey()).get(),
            new BigDecimal(entry.getValue())))
        .collect(Collectors.toSet());
  }

  @RequestMapping(path = "/api/simulation/{id}/lap-reports", method = RequestMethod.GET)
  public Set<LapReport> getStartingBalances(@PathVariable String id,
      @RequestParam(name = "start") Integer start, @RequestParam(name = "pageSize") Integer pageSize) {
    if (start != null) {
      return getSimulationReport(id).getLapReports().stream().skip(start).limit(pageSize)
          .collect(Collectors.toSet());
    }
    return getSimulationReport(id).getLapReports();
  }
}
