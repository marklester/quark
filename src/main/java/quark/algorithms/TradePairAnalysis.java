package quark.algorithms;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import quark.model.TradePair;

public class TradePairAnalysis {
  public void analyze(List<TradePair> tradePairs) {
    Map<String, Integer> fromCounts = Maps.newTreeMap();
    Map<String, Integer> toCounts = Maps.newTreeMap();
    for (TradePair node : tradePairs) {
      String label = node.getLabel();
      List<String> parts = Splitter.on("/").splitToList(label);
      String from = parts.get(0);
      incCounter(fromCounts, from);
      incCounter(toCounts, parts.get(1));
    }
    printCountGroup(fromCounts);
    printCountGroup(toCounts);
  }

  void incCounter(Map<String, Integer> map, String key) {
    map.put(key, (map.getOrDefault(key, 0) + 1));
  }

  void printCountGroup(Map<String, Integer> map) {
    Map<Integer, List<Entry<String, Integer>>> grouped =
        map.entrySet().stream().collect(Collectors.groupingBy(entry -> entry.getValue()));
    for (Entry<Integer, List<Entry<String, Integer>>> entry : grouped.entrySet()) {
      System.out.println(entry);
    }
  }
}
