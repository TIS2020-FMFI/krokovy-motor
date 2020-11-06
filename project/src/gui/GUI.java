package gui;

import gui.chart.Chart;
import measurement.MeasurementManager;
import serialCommunication.SerialCommManager;
import settings.SettingsManager;


public class GUI {

    private final SettingsManager settingsManager; //nastavovanie parametrov

    private final Chart chart; //vykreslovanie grafu

    private final SerialCommManager serialCommManager; //pre priame ovladanie motora

    private final MeasurementManager measurementManager; //snimanie spektra

    public GUI(SettingsManager settingsManager, Chart chart, SerialCommManager serialCommManager, MeasurementManager measurementManager) {
        this.settingsManager = settingsManager;
        this.chart = chart;
        this.serialCommManager = serialCommManager;
        this.measurementManager = measurementManager;
    }


    public void draw(){

    }



}
