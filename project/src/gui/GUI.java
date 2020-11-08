package gui;

import gui.chart.Chart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import measurement.MeasurementManager;
import serialCommunication.SerialCommManager;

import javax.swing.*;


public class GUI {

    private final Chart chart; //vykreslovanie grafu

    private final SerialCommManager serialCommManager; //pre priame ovladanie motora

    private final MeasurementManager measurementManager; //snimanie spektra


    //gui components
    private final Stage primaryStage;

    Scene scene;

    BorderPane mainPane;

    LineChart lineChart;
    HBox topPanel;

    VBox leftPanel;

    Button buttonUP;
    Button buttonDOWN;
    Button buttonRIGHT;
    Button buttonLEFT;

    TextField textFieldForPulses;
    Button moveToAngleButton;

    TextField textFieldForMoveToAngle;

    ObservableList<String> optionsForExpositionTime = FXCollections.observableArrayList(
            "3 ms", "5 ms","10 ms", "20 ms", "50 ms", "100 ms","200 ms", "500 ms","1 s","2 s","5 s","10 s","20 s","30 s","50 s");
    ComboBox<String> comboBoxForExpositionTime;

    Button startButton;
    Button stopButton;

    ToggleGroup angleUnitsButtonsGroup;
    RadioButton gradiansButton;
    RadioButton degreesButton;

    //calibration
    Button startPositionPlusButton;
    Button startPositionMinusButton;
    Button stopPositionPlusButton;
    Button stopPositionMinusButton;
    Button confirmStartButton;
    Button confirmStopButton;
    TextField startPositionTextField;
    TextField stopPositionTextField;





    public GUI(Stage primaryStage, Chart chart, SerialCommManager serialCommManager, MeasurementManager measurementManager) {
        this.chart = chart;
        this.serialCommManager = serialCommManager;
        this.measurementManager = measurementManager;
        this.primaryStage = primaryStage;

        setGuiComponents();
        setLineChart();
        setTopPanel();
        setLeftPanel();

        this.primaryStage.show();

    }


    private void setGuiComponents() {
        mainPane = new BorderPane();
        scene = new Scene(mainPane, 1200, 800);
        mainPane.setPrefSize(1200,800);
        primaryStage.setTitle("Brewster");
        primaryStage.setScene(scene);
    }

    private void setLineChart(){
        scene.getStylesheets().add("gui/chart/style.css");
        lineChart = chart.getComponent();
        mainPane.setCenter(lineChart);
    }

    private void setTopPanel(){
        topPanel = new HBox();

        //logo
        Image logo = new Image("logo1.png");
        ImageView imageView1 = new ImageView(logo);
        topPanel.getChildren().add(imageView1);


        topPanel.getStyleClass().add("toppane");
        mainPane.setTop(topPanel);
    }

    private void setLeftPanel(){
        leftPanel = new VBox();


        //sipky
        GridPane arrowsGrid = new GridPane();

        buttonUP = new Button("+");
        buttonDOWN = new Button("-");
        buttonRIGHT = new Button(">");
        buttonLEFT = new Button("<");
        textFieldForPulses = new TextField();

        textFieldForPulses.setPrefWidth(40);
        buttonUP.setPrefWidth(40);
        buttonDOWN.setPrefWidth(40);

        arrowsGrid.add(buttonUP, 1, 0, 1, 1);
        arrowsGrid.add(textFieldForPulses, 1, 1, 1, 1);
        arrowsGrid.add(buttonDOWN, 1, 2, 1, 1);
        arrowsGrid.add(buttonRIGHT, 2, 1, 1, 1);
        arrowsGrid.add(buttonLEFT, 0, 1, 1, 1);

        leftPanel.getChildren().add(arrowsGrid);

        //move to angle
        GridPane moveToAngleGrid = new GridPane();
        Label label1 = new Label("Move to angle");
        label1.getStyleClass().add("label");
        label1.setPrefWidth(80);
        textFieldForMoveToAngle = new TextField();
        moveToAngleButton = new Button("OK");
        textFieldForMoveToAngle.setPrefWidth(80);


        moveToAngleGrid.add(label1,0,0,1,1);
        moveToAngleGrid.add(textFieldForMoveToAngle,0,1,1,1);
        moveToAngleGrid.add(moveToAngleButton,1,1,1,1);

        leftPanel.getChildren().add(moveToAngleGrid);

        //set exposition time
        GridPane setExpositionTimeGrid = new GridPane();

        Label label2 = new Label("Set exposition time");
        label2.setPrefWidth(200);
        label2.getStyleClass().add("label");

        comboBoxForExpositionTime = new ComboBox<String>(optionsForExpositionTime);
        comboBoxForExpositionTime.setPrefWidth(80);

        setExpositionTimeGrid.add(label2,0,0,1,1);
        setExpositionTimeGrid.add(comboBoxForExpositionTime,0,1,1,1);

        leftPanel.getChildren().add(setExpositionTimeGrid);

        //start stop buttons
        GridPane startStopGrid = new GridPane();
        startButton = new Button("START");
        stopButton = new Button("STOP");
        startButton.setPrefWidth(100);
        stopButton.setPrefWidth(100);
        startButton.getStyleClass().add("startButton");
        stopButton.getStyleClass().add("stopButton");

        startStopGrid.add(startButton,0,0,1,1);
        startStopGrid.add(stopButton,1,0,1,1);

        leftPanel.getChildren().add(startStopGrid);

        //set types of angle units
        GridPane angleUnitsGrid = new GridPane();
        angleUnitsButtonsGroup = new ToggleGroup();
        gradiansButton = new RadioButton("GRADIANS");
        degreesButton = new RadioButton("DEGREES");

        gradiansButton.setToggleGroup(angleUnitsButtonsGroup);
        degreesButton.setToggleGroup(angleUnitsButtonsGroup);

        Label label3 = new Label("Type of angle units");
        label3.getStyleClass().add("label");
        angleUnitsGrid.add(label3,0,0,1,1);
        angleUnitsGrid.add(gradiansButton,0,1,1,1);
        angleUnitsGrid.add(degreesButton,1,1,1,1);

        leftPanel.getChildren().add(angleUnitsGrid);

        //calibration
        GridPane calibrationGrid = new GridPane();
        Label label4 = new Label("CALIBRATION");
        label4.getStyleClass().add("label");
        Label labelStart = new Label("START");
        labelStart.getStyleClass().add("label");
        Label labelStop = new Label("STOP");
        labelStop.getStyleClass().add("label");
        startPositionPlusButton = new Button("+");
        startPositionMinusButton = new Button("-");
        stopPositionPlusButton = new Button("+");
        stopPositionMinusButton = new Button("-");
        confirmStartButton = new Button("CONFIRM START");
        confirmStopButton = new Button("CONFIRM STOP ");
        startPositionTextField = new TextField();
        stopPositionTextField = new TextField();

        startPositionTextField.setPrefWidth(80);
        stopPositionTextField.setPrefWidth(80);

        calibrationGrid.add(label4,0,0,1,1);
        calibrationGrid.add(labelStart,0,1,1,1);
        calibrationGrid.add(startPositionMinusButton,1,1,1,1);
        calibrationGrid.add(startPositionTextField,2,1,1,1);
        calibrationGrid.add(startPositionPlusButton,3,1,1,1);
        calibrationGrid.add(confirmStartButton,4,1,1,1);

        calibrationGrid.add(labelStop,0,2,1,1);
        calibrationGrid.add(stopPositionMinusButton,1,2,1,1);
        calibrationGrid.add(stopPositionTextField,2,2,1,1);
        calibrationGrid.add(stopPositionPlusButton,3,2,1,1);
        calibrationGrid.add(confirmStopButton,4,2,1,1);

        leftPanel.getChildren().add(calibrationGrid);

        //set to main pane
        leftPanel.setSpacing(20);
        leftPanel.getStyleClass().add("leftpane");
        mainPane.setLeft(leftPanel);

    }


    public void draw() {

    }


}
