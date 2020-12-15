package gui.chart;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * trieda na kreslenie grafov. treba dat set xvalues a yvalues
 */
public class Chart {
    private final double drawingTime = 20; //odhadovany cas nakreslenie

    private double[] xValues;
    private double[] yValues;

    //create axis
    NumberAxis xAxis =  new NumberAxis(200, 850, 10);
    NumberAxis yAxis = new NumberAxis();
    LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);

    XYChart.Series mainData;  //hlavna series, ktorej data sa budu replaceovat

    public Chart(File inputFile){ //nacitanie z matice
        lineChart.setCreateSymbols(false);
        lineChart.getStyleClass().add("thick-chart"); //styl z css suboru

        List<Double> xValuesList = new ArrayList<>();
        List<Double> yValuesList = new ArrayList<>();
        try(Scanner scanner = new Scanner(inputFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] splitLine = line.split("\\s+");
                xValuesList.add(Double.valueOf(splitLine[0]));
                yValuesList.add(Double.valueOf(splitLine[1]));
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e2){
            System.out.println("File is in wrong format");
        }

        mainData = new XYChart.Series();

        xValues = new double[xValuesList.size()];
        for (int i = 0; i < xValues.length; i++) {
            xValues[i] = xValuesList.get(i);
        }
        yValues = new double[yValuesList.size()];
        for (int i = 0; i < yValues.length; i++) {
            yValues[i] = yValuesList.get(i);
        }
        fillMainData();
    }

    public Chart(double[] xValues, String xAxisLabel, String yAxisLabel, String chartTitle) {
        this.xValues = xValues;

        if (xAxisLabel != null) xAxis.setLabel(xAxisLabel);
        if (yAxisLabel != null) yAxis.setLabel(yAxisLabel);

        //creating the gui.chart
        if (chartTitle != null) lineChart.setTitle(chartTitle);
        //set suitable parameters
        lineChart.setCreateSymbols(false);
        lineChart.setAnimated(false);
        lineChart.getStyleClass().add("thick-chart"); //styl z css suboru

        //defining a series
        mainData = new XYChart.Series();
        mainData.setName(chartTitle);

        if (yValues == null) {
            yValues = new double[650];
        }

        if (this.xValues == null) {
            this.xValues = new double[650];
        }
        fillMainData();
    }

    private void fillMainData(){
        //populating the series with data
        for (int i = 0; i < Math.min(xValues.length, yValues.length); i++) {
            mainData.getData().add(new XYChart.Data(xValues[i], yValues[i]));
        }
        mainData.setName("Data");
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
        for (int i = 0; i < Math.min(xValues.length, yValues.length); i++) {
            newData.getData().add(new XYChart.Data(xValues[i], yValues[i]));
        }

        lineChart.getData().remove(mainData);
        lineChart.getData().add(newData);
        newData.setName(dataTitle);
        mainData = newData;
    }


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

    public double getDrawingTime() {
        return drawingTime;
    }
}
