package measurement;

import Exceptions.FilesAndFoldersExcetpions.ParameterIsNullException;
import serialCommunication.StepperMotor;
import com.oceanoptics.omnidriver.api.wrapper.Wrapper;
import gui.chart.Chart;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import settings.Settings;



public class MeasurementManager {

    private Timeline livemodeTimeline;
    private Timeline seriesOfMTimeline;
    private double currentAngle;

    public Wrapper wrapper;

    private StepperMotor stepperMotor;

    public MeasurementManager(StepperMotor stepperMotor) {
        this.stepperMotor = stepperMotor;
        wrapper = new Wrapper();
    }

    public void startLiveMode(Integer integrationTime, Chart chart){
        Double interval = integrationTime + chart.getDrawingTime();
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

    public void startSeriesOfMeasurements(Chart chart, Double currentAngle){
        Double interval = Settings.getIntegrationTime() + chart.getDrawingTime() + Settings.getStepSize() * stepperMotor.getImpulseTime();
        Double startAngle = Settings.getMeasurementMinAngle();
        Double endAngle = Settings.getCalibrationMaxAngle();
        Double stepToAngleRatio = Settings.getStepToAngleRatio();
        this.currentAngle = currentAngle;   //nemusi sa nam podarit dostat presne na startAngle
        if(currentAngle > endAngle) return;

        Integer stepsToDo = stepperMotor.stepsNeededToMove(currentAngle, endAngle);

        double[] wavelengths = wrapper.getWavelengths(0);
        chart.setxValues(wavelengths);
        wrapper.setIntegrationTime(0, Settings.getIntegrationTime());

        SeriesOfMeasurements seriesOfMeasurements = new SeriesOfMeasurements();
        seriesOfMTimeline = new Timeline(new KeyFrame(Duration.millis(interval), e -> {
            double[] spectralData = wrapper.getSpectrum(0);
            chart.replaceMainData(spectralData, "last measured data");
            try {
                seriesOfMeasurements.addMeasurement(new Measurement(spectralData, wavelengths, this.currentAngle));
            } catch (ParameterIsNullException parameterIsNullException) {
                parameterIsNullException.printStackTrace();
            }
            this.currentAngle += stepToAngleRatio; //currentAngle musi byt triedny param. kvoli timeline
            if(startAngle < endAngle){
                stepperMotor.stepForward();
            }
            else{
                stepperMotor.stepBackwards();
            }
        }));

        seriesOfMTimeline.setCycleCount(stepsToDo);
        seriesOfMTimeline.play();

        seriesOfMTimeline.setOnFinished(e -> {
            try {
                seriesOfMeasurements.save();
            } catch (ParameterIsNullException parameterIsNullException) {
                parameterIsNullException.printStackTrace();
            }
        });
    }

    public void stopSeriesOfMeasurements(){
        seriesOfMTimeline.stop();
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
