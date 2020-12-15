package measurement;

import Exceptions.SerialCommunicationExceptions.PicaxeConnectionErrorException;
import Exceptions.SpectrometerExceptions.SpectrometerNotConnected;
import gui.RemainingStepsObserver;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import serialCommunication.Spectrometer;
import serialCommunication.StepperMotor;
import com.oceanoptics.omnidriver.api.wrapper.Wrapper;
import gui.chart.Chart;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import settings.Settings;

import java.io.File;


public class MeasurementManager {

    private Timeline livemodeTimeline;

    public Wrapper wrapper;
    private Spectrometer spectrometer;
    private StepperMotor stepperMotor;


    public MeasurementManager(StepperMotor stepperMotor) {
        this.stepperMotor = stepperMotor;
        wrapper = new Wrapper();
        spectrometer = new Spectrometer(wrapper);
    }

    public void startLiveMode(Integer integrationTime, Chart chart) {
        Double minInterval = 200.0;
        Double interval = Math.max(minInterval, (integrationTime / 1000 * Settings.getInstance().getNumberOfScansToAverage()) + chart.getDrawingTime());
        chart.setxValues(wrapper.getWavelengths(0));
        wrapper.setScansToAverage(0, Settings.getInstance().getNumberOfScansToAverage());
        wrapper.setIntegrationTime(0, integrationTime);

        livemodeTimeline = new Timeline(new KeyFrame(Duration.millis(interval), e -> {
            chart.replaceMainData(wrapper.getSpectrum(0), "current data");
        }));
        livemodeTimeline.setCycleCount(Timeline.INDEFINITE);
        livemodeTimeline.play();
    }

    public void stopLiveMode() {
        livemodeTimeline.stop();
    }

    public void startSeriesOfMeasurements(Chart chart, Label remainingStepsLabel) throws PicaxeConnectionErrorException, SpectrometerNotConnected {
        SeriesOfMeasurements sofm = new SeriesOfMeasurements(wrapper, stepperMotor, spectrometer, this);
        RemainingStepsObserver remainingStepsObserver = new RemainingStepsObserver(sofm, remainingStepsLabel);
        sofm.attach(remainingStepsObserver);
        sofm.begin(chart);
    }

    public void measureBackground() {
        wrapper.setScansToAverage(0, Settings.getInstance().getNumberOfScansToAverage());
        Settings.getInstance().setBackground(wrapper.getSpectrum(0));
    }

    public void checkConnectionOfSpectrometer() throws SpectrometerNotConnected {
        spectrometer.checkConnection();
    }

    public void showSelectedChart(){
        Stage secondStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        File measuredDataDir = new File("measuredData"); //../measuredData - ak sa spusta cez bat, treba toto zmenit

        if(measuredDataDir.exists() == false){
            return;
        }

        fileChooser.setInitialDirectory(measuredDataDir);

        File inputFile;
        try{
            inputFile = fileChooser.showOpenDialog(secondStage);
        } catch (IllegalArgumentException ex){
            System.out.println(ex.getMessage());
            return;
        }
        if (inputFile != null) {
            Chart chart = new Chart(inputFile);
            LineChart chartComponent = chart.getComponent();
            chartComponent.setPrefSize(900,600);
            Scene scene = new Scene(new HBox(3, chartComponent));
            scene.getStylesheets().add("gui/chart/style.css");
            secondStage.setScene(scene);
            secondStage.show();
        }
    }
}
