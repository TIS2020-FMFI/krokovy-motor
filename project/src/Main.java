import gui.chart.Chart;
import gui.GUI;
import javafx.application.Application;
import javafx.stage.Stage;
import measurement.MeasurementManager;
import serialCommunication.StepperMotor;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Chart chart = new Chart(null, null, null, null);
        StepperMotor stepperMotor = new StepperMotor();
        GUI gui = new GUI(primaryStage, chart, stepperMotor, new MeasurementManager(stepperMotor));
    }
}
