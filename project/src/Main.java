import gui.GUI;
import gui.chart.Chart;
import javafx.application.Application;
import javafx.scene.chart.LineChart;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Chart chart = new Chart(null,null,null,null);
        GUI gui = new GUI(primaryStage, chart,null,null);
    }
}
