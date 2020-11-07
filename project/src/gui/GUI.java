package gui;

import gui.chart.Chart;
import measurement.MeasurementManager;
import serialCommunication.SerialCommManager;


public class GUI {

    private final Chart chart; //vykreslovanie grafu

    private final SerialCommManager serialCommManager; //pre priame ovladanie motora

    private final MeasurementManager measurementManager; //snimanie spektra

    public GUI(Chart chart, SerialCommManager serialCommManager, MeasurementManager measurementManager) {
        this.chart = chart;
        this.serialCommManager = serialCommManager;
        this.measurementManager = measurementManager;
    }


    public void draw(){

    }



}
