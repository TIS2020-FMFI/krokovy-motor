package measurement;

import serialCommunication.SerialCommManager;
import settings.SettingsManager;
import spectrometer.SpectrometerWrapper;
import com.oceanoptics.omnidriver.api.wrapper.Wrapper;
import gui.chart.ChartManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class MeasurementManager {

    private Timeline timeline;

    public Wrapper wrapper;

    private SerialCommManager serialCommManager;

    private SettingsManager settingsManager;

    public MeasurementManager(SerialCommManager serialCommManager, SettingsManager settingsManager) {
        this.serialCommManager = serialCommManager;
        this.settingsManager = settingsManager;
        wrapper = SpectrometerWrapper.getInstance();
    }

    public void livemode(double interval, ChartManager chartManager){
        timeline = new Timeline(new KeyFrame(Duration.millis(interval), e -> {
            chartManager.replaceMainData(wrapper.getSpectrum(0), "current data");
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void startSeriesOfMeasurements(){
        SeriesOfMeasurements seriesOfMeasurements = new SeriesOfMeasurements();
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void stop(){
        timeline.stop();
    }

    public static void main(String[] args) {
//        MeasurementManager mm = new MeasurementManager(serialCommManager);
//        ChartManager chartManager = new ChartManager(mm.wrapper.getWavelengths(0),"wavelengths","intensities","Test");
//        LineChart lineChart = chartManager.getComponent();
//        VBox vbox = new VBox(lineChart);
//
//        Scene scene  = new Scene(vbox,1500,600);
//        scene.getStylesheets().add("style.css");
//        /*primaryStage.setTitle("chartTest"); 	// pomenuj okno aplikacie, javisko
//        primaryStage.setScene(scene); 			// vloz scenu do hlavneho okna, na javisko
//        primaryStage.show();*/
    }
}
