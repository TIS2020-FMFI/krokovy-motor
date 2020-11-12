package measurement;

import serialCommunication.StepperMotor;
import com.oceanoptics.omnidriver.api.wrapper.Wrapper;
import gui.chart.Chart;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class MeasurementManager {

    private Timeline timeline;

    public Wrapper wrapper;

    private StepperMotor stepperMotor;

    public MeasurementManager(StepperMotor stepperMotor) {
        this.stepperMotor = stepperMotor;
        wrapper = new Wrapper();
    }

    public void livemode(double interval, Chart chart){
        timeline = new Timeline(new KeyFrame(Duration.millis(interval), e -> {
            chart.replaceMainData(wrapper.getSpectrum(0), "current data");
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
