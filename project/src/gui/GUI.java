package gui;

import gui.chart.ChartManager;
import measurement.MeasurementManager;
import serialCommunication.SerialCommManager;
import settings.SettingsManager;
import spectrometer.SpectrometerWrapper;
import com.oceanoptics.omnidriver.api.wrapper.Wrapper;


public class GUI {

    private final SettingsManager settingsManager; //nastavovanie parametrov

    private final ChartManager chartManager; //vykreslovanie grafu

    private final SerialCommManager serialCommManager; //pre priame ovladanie motora

    private final MeasurementManager measurementManager; //snimanie spektra

    public GUI(SettingsManager settingsManager, ChartManager chartManager, SerialCommManager serialCommManager, MeasurementManager measurementManager) {
        this.settingsManager = settingsManager;
        this.chartManager = chartManager;
        this.serialCommManager = serialCommManager;
        this.measurementManager = measurementManager;
    }


    public void draw(){

    }



}
