import spectrometer.SpectrometerWrapper;
import com.oceanoptics.omnidriver.api.wrapper.Wrapper;
import gui.chart.ChartManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import spectrometer.SpectrometerWrapper;

public class Test extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    ChartManager chartManager;

    @Override
    public void start(Stage primaryStage)  {
        ChartManager chartManager = new ChartManager(null,null,null,null);

        LineChart lineChart = chartManager.getComponent();
        VBox vbox = new VBox(lineChart);

        Scene scene  = new Scene(vbox,1500,600);
        scene.getStylesheets().add("style.css");  //treba kvoli grafu

        primaryStage.setTitle("chartTest"); 	// pomenuj okno aplikacie, javisko
        primaryStage.setScene(scene); 			// vloz scenu do hlavneho okna, na javisko
        primaryStage.show();

        Wrapper wrapper = new Wrapper();//SpectrometerWrapper.getInstance();
        chartManager.setxValues(wrapper.getWavelengths(0));

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(300),
                e -> {
                    chartManager.replaceMainData(wrapper.getSpectrum(0), "Data");
                }));
        timeline.setCycleCount(20);
        timeline.play();
    }

}
