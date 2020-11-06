import com.oceanoptics.omnidriver.api.wrapper.Wrapper;
import gui.chart.Chart;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Test extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    Chart chart;

    @Override
    public void start(Stage primaryStage)  {
        Chart chart = new Chart(null,null,null,null);

        LineChart lineChart = chart.getComponent();
        VBox vbox = new VBox(lineChart);

        Scene scene  = new Scene(vbox,1500,600);
        scene.getStylesheets().add("style.css");  //treba kvoli grafu

        primaryStage.setTitle("chartTest"); 	// pomenuj okno aplikacie, javisko
        primaryStage.setScene(scene); 			// vloz scenu do hlavneho okna, na javisko
        primaryStage.show();

        Wrapper wrapper = new Wrapper();//SpectrometerWrapper.getInstance();
        int numberOfSpectrometers = wrapper.openAllSpectrometers();

        if(numberOfSpectrometers == 0){
            System.out.println("No spectrometer found");
            return;
        }
        wrapper.setIntegrationTime(0, 50);  //spektrometer s indexom 0, 50ms

        chart.setxValues(wrapper.getWavelengths(0));

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(300),
                e -> {
                    chart.replaceMainData(wrapper.getSpectrum(0), "Data");
                }));
        timeline.setCycleCount(20);
        timeline.play();
    }

}
