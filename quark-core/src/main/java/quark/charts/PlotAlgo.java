package quark.charts;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

public class PlotAlgo{
  private List<Series<LocalDateTime,? extends Number>> series;

  public PlotAlgo(List<Series<LocalDateTime,? extends Number>> series) {
    this.series = series;
  }
  private DateTimeFormatter dtFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
  public void start(Stage stage) {
      stage.setTitle("Line Chart Sample");
      //defining the axes
      final CategoryAxis xAxis = new CategoryAxis();
      final NumberAxis yAxis = new NumberAxis();
      xAxis.setLabel("Number of Month");
      //creating the chart
      final LineChart<String,Number> lineChart = 
              new LineChart<String,Number>(xAxis,yAxis);
      lineChart.setTitle("Quark Plot"); 
      
      for(Series<LocalDateTime,? extends Number> s: series) {
        Series<String, Number> convertedSeries = new Series<>();
        convertedSeries.setName(s.getName());
        for(Data<LocalDateTime, ? extends Number> pt: s.getData()) {
          String dt = pt.getXValue().format(dtFormatter);
          convertedSeries.getData().add(new Data<String,Number>(dt, pt.getYValue()));
        }
        lineChart.getData().add(convertedSeries);
      }

      Scene scene  = new Scene(lineChart,800,600);
     
      stage.setScene(scene);
      stage.show();
  }
}