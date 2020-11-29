package measurement;

import Exceptions.FilesAndFoldersExcetpions.ParameterIsNullException;
import Exceptions.SerialCommunicationExceptions.PicaxeConnectionErrorException;
import Exceptions.SpectrometerExceptions.SpectrometerNotConnected;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
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
    private Timeline seriesOfMTimeline;
    private int remainingSteps;

    public Wrapper wrapper;

    private StepperMotor stepperMotor;

    public SpectroSimulator spectroSimulator;


    public MeasurementManager(StepperMotor stepperMotor) {
        this.stepperMotor = stepperMotor;
        wrapper = new Wrapper();
        spectroSimulator = new SpectroSimulator(200,800);
    }

    public void startLiveMode(Integer integrationTime, Chart chart){
        Double minInterval = 200.0;
        Double interval = Math.max(minInterval, (integrationTime/1000) + chart.getDrawingTime());
        chart.setxValues(wrapper.getWavelengths(0));
        wrapper.setIntegrationTime(0, integrationTime);

        livemodeTimeline = new Timeline(new KeyFrame(Duration.millis(interval), e -> {
            chart.replaceMainData(wrapper.getSpectrum(0), "current data");
        }));
        livemodeTimeline.setCycleCount(Timeline.INDEFINITE);
        livemodeTimeline.play();
    }

    public void startSimulatedLiveMode(Integer integrationTime, Chart chart){
        spectroSimulator = new SpectroSimulator(200,800);
        Double interval = ((integrationTime/1000) + chart.getDrawingTime());
        chart.setxValues(spectroSimulator.getWaveLengths());

        livemodeTimeline = new Timeline(new KeyFrame(Duration.millis(interval), e -> {
            chart.replaceMainData(spectroSimulator.getSpectrum(), "current data");
        }));
        livemodeTimeline.setCycleCount(Timeline.INDEFINITE);
        livemodeTimeline.play();
    }

    public void stopLiveMode(){
        livemodeTimeline.stop();
    }

    public void seriesOfMeasurements(Chart chart, Label currentAngleLabel, Label remainingStepsLabel) throws PicaxeConnectionErrorException, SpectrometerNotConnected {
        if(stepperMotor.checkPicaxeConnection() == false){
            throw new PicaxeConnectionErrorException("Picaxe is not connected");
        }
        checkConnectionOfSpectrometer();
        startSeriesOfMeasurements(chart, currentAngleLabel, remainingStepsLabel);

    }

    private void startSeriesOfMeasurements(Chart chart, Label currentAngleLabel, Label remainingStepsLabel) {
        Double interval = (Settings.getIntegrationTime()/1000) + chart.getDrawingTime() + stepperMotor.getStepTime();
        Double startAngle = Settings.getMeasurementMinAngle();
        Double endAngle = Settings.getMeasurementMaxAngle();

        Integer stepsToDo = stepperMotor.stepsNeededToMove(endAngle);
        remainingSteps = stepsToDo;
        remainingStepsLabel.setText(String.valueOf(remainingSteps));

        double[] wavelengths = wrapper.getWavelengths(0);
        chart.setxValues(wavelengths);
        wrapper.setIntegrationTime(0, Settings.getIntegrationTime());
        wrapper.setScansToAverage(0, Settings.getNumberOfScansToAverage());

        SeriesOfMeasurements seriesOfMeasurements = new SeriesOfMeasurements();
        seriesOfMTimeline = new Timeline(new KeyFrame(Duration.millis(interval), e -> {

            measureAndVisualize(chart, wavelengths, seriesOfMeasurements);

            if (startAngle < endAngle){
                stepperMotor.stepForward(currentAngleLabel);
            } else {
                stepperMotor.stepBackwards(currentAngleLabel);
            }
            remainingSteps --;
            remainingStepsLabel.setText(String.valueOf(remainingSteps));
        }));

        seriesOfMTimeline.setCycleCount(stepsToDo);
        seriesOfMTimeline.play();
        seriesOfMTimeline.setOnFinished(e -> {
            try {
                measureAndVisualize(chart, wavelengths, seriesOfMeasurements); //odmeriam aj na konci intervalu
                seriesOfMeasurements.save();
                startLiveMode(Settings.getIntegrationTime(), chart);
            } catch (ParameterIsNullException parameterIsNullException) {
                parameterIsNullException.printStackTrace();
            }
        });
    }

    public void stopSeriesOfMeasurements(){
        seriesOfMTimeline.stop();
    }

    private void measureAndVisualize(Chart chart, double[] wavelengths, SeriesOfMeasurements sofm){
        double[] spectralData = wrapper.getSpectrum(0);
        substractBackgroundIfNeeded(spectralData);
        chart.replaceMainData(spectralData, "last measured data");
        try {
            sofm.addMeasurement(new Measurement(spectralData, wavelengths, stepperMotor.currentAngle));
        } catch (ParameterIsNullException parameterIsNullException) {
            parameterIsNullException.printStackTrace();
        }
    }

    public void measureBackground(){
        Settings.setBackground(wrapper.getSpectrum(0));
    }

    private void substractBackgroundIfNeeded(double[] values){
        double[] background = Settings.getBackground();
        if(Settings.getSubtractBackground() == false || background == null){
            return;
        }
        for (int i = 0; i < Math.min(values.length, background.length); i++) {
            values[i] = Math.max(values[i] - background[i], 0);
        }
    }

    public void checkConnectionOfSpectrometer() throws SpectrometerNotConnected {
        if(wrapper == null){
            throw new SpectrometerNotConnected("Spectrometer is not connected");
        }

        int numberOfSpectrometers;
        try{
            numberOfSpectrometers = wrapper.openAllSpectrometers();
        } catch (java.lang.ExceptionInInitializerError | java.lang.NoClassDefFoundError e){
            throw new SpectrometerNotConnected("Spectrometer is not connected");
        }

        if(numberOfSpectrometers == -1){ //nejaka specialna chyba
            throw new SpectrometerNotConnected(wrapper.lastException.getMessage());
        }
        if(numberOfSpectrometers == 0){
            throw new SpectrometerNotConnected("Spectrometer is not connected");
        }
        if(numberOfSpectrometers > 1){
            throw new SpectrometerNotConnected("Multiple spectrometers are connected");
        }
    }

//    public static void main(String[] args) {
////        MeasurementManager mm = new MeasurementManager(serialCommManager);
////        ChartManager chartManager = new ChartManager(mm.wrapper.getWavelengths(0),"wavelengths","intensities","Test");
////        LineChart lineChart = chartManager.getComponent();
////        VBox vbox = new VBox(lineChart);
////
////        Scene scene  = new Scene(vbox,1500,600);
////        scene.getStylesheets().add("style.css");
////        /*primaryStage.setTitle("chartTest"); 	// pomenuj okno aplikacie, javisko
////        primaryStage.setScene(scene); 			// vloz scenu do hlavneho okna, na javisko
////        primaryStage.show();*/
//    }
}
