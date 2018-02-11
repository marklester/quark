package quark.algorithms;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;

import quark.balance.BalanceListing;
import quark.charts.PlotlyTrace;
import quark.orders.ProcessedOrder;

public class SimulationReport {
  private final String id;
  private NavigableSet<LapReport> lapReports = new ConcurrentSkipListSet<>();
  private final AtomicBoolean complete = new AtomicBoolean(false);

  public SimulationReport(String id) {
    this.id = id;
  }

  public Set<LapReport> getLapReports() {
    return lapReports;
  }

  public String getId() {
    return id;
  }

  public void addLapReport(LapReport lapReport) {
    lapReports.add(lapReport);
  }

  public void complete() {
    getComplete().set(true);
  }

  public AtomicBoolean getComplete() {
    return complete;
  }
 
  @JsonIgnore
  public Collection<PlotlyTrace> getPlots() {
    
    if(lapReports.isEmpty()) {
      return Collections.emptyList();
    }
    
    LapReport lastReport = lapReports.last();
    BalanceListing listing = lastReport.getBalanceListing();
    
    if (listing == null) {
      return Collections.emptyList();
    }
    Set<String> coinNames = listing.getBalances().stream().map(b -> b.getCurrency().getName())
        .collect(Collectors.toSet());

    Map<CoinKey, PlotlyTrace> traces = Maps.newHashMap();
    for (LapReport report : lapReports) {
      for (DataPoint pt : report.getDataPoints()) {
        if (coinNames.contains(pt.getLabel().coin)) {
          PlotlyTrace trace = traces.computeIfAbsent(pt.getLabel(), k -> PlotlyTrace.of(pt));
          trace.add(report.getDateTime(), pt.getValue().doubleValue());
        }
      }
    }
    return traces.values();
  }
  
  @JsonIgnore
  public Set<ProcessedOrder> getProcessedOrders() {
    return lapReports.stream().map(r -> r.getProcessedOrders()).flatMap(Set::stream)
        .collect(Collectors.toSet());
  }
}
