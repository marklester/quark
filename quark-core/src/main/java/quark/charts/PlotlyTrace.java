package quark.charts;

import java.time.LocalDateTime;
import java.util.List;

import com.google.common.collect.Lists;

import quark.algorithms.CoinKey;
import quark.algorithms.DataPoint;

public class PlotlyTrace {
  public static PlotlyTrace bar(CoinKey name) {
    return new PlotlyTrace(name.toString(), PlotType.bar);
  }
  
  public static PlotlyTrace line(CoinKey name) {
    return new PlotlyTrace(name.toString(), PlotType.scatter);
  }
  
  public static PlotlyTrace of(DataPoint pt) {
    return new PlotlyTrace(pt.getLabel().toString(), pt.getType());
  }
  
  public enum PlotType{
     scatter,bar;
  }
  private String name;
  private PlotType type;
  private List<LocalDateTime> x = Lists.newArrayList();
  private List<Number> y = Lists.newArrayList();
  
  public PlotlyTrace(String name,PlotType type) {
    this.name = name;
    this.type = type;
  }
  
  public List<LocalDateTime> getX() {
    return x;
  }

  public void setX(List<LocalDateTime> x) {
    this.x = x;
  }

  public List<Number> getY() {
    return y;
  }

  public void setY(List<Number> y) {
    this.y = y;
  }

  public PlotType getType() {
    return type;
  }

  public void setType(PlotType type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void add(LocalDateTime ptx, double pty) {
    x.add(ptx);
    y.add(pty);    
  }
}

