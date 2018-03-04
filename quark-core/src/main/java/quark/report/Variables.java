package quark.report;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import quark.algorithms.Algorithm;

/**
 * used to store variables calculated for each market in {@link LapReport} for an {@link Algorithm}
 *
 */
public class Variables {
  Table<Integer, String, Double> variables = HashBasedTable.create();

  public Double getVariable(Integer tradePairId, String variableName) {
    return variables.get(tradePairId, variableName);
  }

  public void putVariable(Integer tradePairId, String variableName, Double value) {
    variables.put(tradePairId, variableName, value);
  }
}
