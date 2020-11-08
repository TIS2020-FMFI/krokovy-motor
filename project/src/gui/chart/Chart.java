package gui.chart;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * trieda na kreslenie grafov. treba dat set xvalues a yvalues
 */
public class Chart {
    private double[] xValues;
    private double[] yValues;

    final NumberAxis xAxis = new NumberAxis(200, 850, 10);
    final NumberAxis yAxis = new NumberAxis(0, 200, 10);
    final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);

    XYChart.Series mainData;  //hlavna series, ktorej data sa budu replaceovat

    public Chart(double[] xValues, String xAxisLabel, String yAxisLabel, String chartTitle) {
        this.xValues = xValues;

        //create axis
        final NumberAxis xAxis = new NumberAxis(200, 850, 10);
        final NumberAxis yAxis = new NumberAxis(0, 200, 10);
        if (xAxisLabel != null) xAxis.setLabel(xAxisLabel);
        if (yAxisLabel != null) yAxis.setLabel(yAxisLabel);

        //creating the gui.chart
        if (chartTitle != null) lineChart.setTitle(chartTitle);
        //set suitable parameters
        lineChart.setCreateSymbols(false);
//        lineChart.setMaxWidth(800);
        lineChart.setMaxHeight(400);
//        lineChart.setPrefWidth(800); //1500
//        lineChart.setPrefHeight(400);
        lineChart.setAnimated(false);
        lineChart.getStyleClass().add("thick-chart"); //styl z css suboru

        //defining a series
        mainData = new XYChart.Series();
        mainData.setName(chartTitle);

        if (yValues == null) {
            yValues = new double[650];
        }

        if (xValues == null) {
            xValues = new double[650];
        }
        //populating the series with data
        for (int i = 0; i < xValues.length; i++) {
            mainData.getData().add(new XYChart.Data(xValues[i], yValues[i]));
        }

        lineChart.getData().add(mainData);
    }

    /**
     * sluzi na vytvorenie komponentu linechart ktory sa umiestni do nejakeho pane-u
     *
     * @return linechart komponent
     */
    public LineChart getComponent() {
        return lineChart;
    }

    /**
     * prida novu ciaru do grafu
     */
    public void addNewData(double[] yValues, String dataTitle) {
        XYChart.Series series1 = new XYChart.Series();
        for (int i = 0; i < xValues.length; i++) {
            series1.getData().add(new XYChart.Data(xValues[i], yValues[i]));
        }

        lineChart.getData().add(series1);
        if (dataTitle != null) series1.setName(dataTitle);
    }

    /**
     * premietne polia xValues a zadane yValues do grafu
     */
    public void replaceMainData(double[] yValues, String dataTitle) {
        setyValues(yValues);

        XYChart.Series newData = new XYChart.Series();
//        for (int i = 0; i < xValues.length; i++) {
//            newData.getData().add(new XYChart.Data(xValues[i], yValues[i]));
//        }

        lineChart.getData().remove(mainData);
        lineChart.getData().add(newData);
        newData.setName(dataTitle);
        mainData = newData;
    }

    //mozno tak sa daju updatovat values
//https://stackoverflow.com/questions/21876073/update-values-in-line-chart

    public void setxValues(double[] xValues) {
        this.xValues = xValues;
    }

    public void setyValues(double[] yValues) {
        this.yValues = yValues;
    }

    public void setXYvalues(double[] xValues, double[] yValues) {
        this.xValues = xValues;
        this.yValues = yValues;
    }
}
