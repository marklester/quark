package quark.report;

import java.util.Map;

public class SimParams {
  private String tickRate;
  private String shortAvg;
  private String longAvg;
  private Integer portfolioSize;
  private Map<String, String> startingBalances;
  
  public String getTickRate() {
    return tickRate;
  }

  public void setTickRate(String tickRate) {
    this.tickRate = tickRate;
  }

  public String getShortAvg() {
    return shortAvg;
  }

  public void setShortAvg(String shortAvg) {
    this.shortAvg = shortAvg;
  }

  public String getLongAvg() {
    return longAvg;
  }

  public void setLongAvg(String longAvg) {
    this.longAvg = longAvg;
  }

  public Integer getPortfolioSize() {
    return portfolioSize;
  }

  public void setPortfolioSize(Integer portfolioSize) {
    this.portfolioSize = portfolioSize;
  }

  public Map<String, String> getStartingBalances() {
    return startingBalances;
  }

  public void setStartingBalances(Map<String, String> startingBalances) {
    this.startingBalances = startingBalances;
  }
}
