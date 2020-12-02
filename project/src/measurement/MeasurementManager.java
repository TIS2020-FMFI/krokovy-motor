package measurement;

import Exceptions.FilesAndFoldersExcetpions.ParameterIsNullException;
import Exceptions.SerialCommunicationExceptions.PicaxeConnectionErrorException;
import Exceptions.SpectrometerExceptions.SpectrometerNotConnected;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import serialCommunication.Spectrometer;
import serialCommunication.StepperMotor;
import com.oceanoptics.omnidriver.api.wrapper.Wrapper;
import gui.chart.Chart;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import settings.Settings;
import spectroSimulator.SpectroSimulator;

import java.util.Set;


public class MeasurementManager {

    private Timeline livemodeTimeline;
    public Integer remainingSteps;

    public Wrapper wrapper;
    private Spectrometer spectrometer;
    private StepperMotor stepperMotor;

    //public SpectroSimulator spectroSimulator;


    public MeasurementManager(StepperMotor stepperMotor) {
        this.stepperMotor = stepperMotor;
        wrapper = new Wrapper();
        spectrometer = new Spectrometer(wrapper);
        //spectroSimulator = new SpectroSimulator(200,800);
    }

    public void startLiveMode(Integer integrationTime, Chart chart){
        Double minInterval = 200.0;
        Double interval = Math.max(minInterval, (integrationTime/1000 * Settings.getInstance().getNumberOfScansToAverage()) + chart.getDrawingTime());
        chart.setxValues(wrapper.getWavelengths(0));
        wrapper.setIntegrationTime(0, integrationTime);

        livemodeTimeline = new Timeline(new KeyFrame(Duration.millis(interval), e -> {
            chart.replaceMainData(wrapper.getSpectrum(0), "current data");
        }));
        livemodeTimeline.setCycleCount(Timeline.INDEFINITE);
        livemodeTimeline.play();
    }

    public void stopLiveMode(){
        livemodeTimeline.stop();
    }
/*
    public void startSimulatedLiveMode(Integer integrationTime, Chart chart){
        spectroSimulator = new SpectroSimulator(200,800);
        Double interval = ((integrationTime/1000) + chart.getDrawingTime());
        chart.setxValues(spectroSimulator.getWaveLengths());

        livemodeTimeline = new Timeline(new KeyFrame(Duration.millis(interval), e -> {
            chart.replaceMainData(spectroSimulator.getSpectrum(), "current data");
        }));
        livemodeTimeline.setCycleCount(Timeline.INDEFINITE);
        livemodeTimeline.play();
    }*/

    public void startSeriesOfMeasurements(Chart chart, Label remainingStepsLabel) throws PicaxeConnectionErrorException, SpectrometerNotConnected {
        SeriesOfMeasurements sofm = new SeriesOfMeasurements(wrapper, stepperMotor, spectrometer, this);
        sofm.begin(chart, remainingStepsLabel);
    }

    public void measureBackground(){
        Settings.getInstance().setBackground(wrapper.getSpectrum(0));
    }

    public void checkConnectionOfSpectrometer() throws SpectrometerNotConnected {
        spectrometer.checkConnection();
    }

}
