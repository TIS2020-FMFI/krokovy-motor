package gui;

import gui.chart.Chart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import measurement.MeasurementManager;
import serialCommunication.SerialCommManager;


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
    HBox infoPanel;
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

    //config mode
    ToggleGroup modeButtonsGroup;
    RadioButton currentModeButton;
    RadioButton ltAvgModeButton;
    Button measureCountPlusButton;
    Button measureCountMinusButton;
    TextField measureCountTextField;

    //config wavelength range
    Button waveBottomPlusButton;
    Button waveBottomMinusButton;
    TextField waveBottomTextField;

    Button waveTopPlusButton;
    Button waveTopMinusButton;
    TextField waveTopTextField;

    //noise
    Button measureNoiseButton;
    CheckBox applyNoiseButton;

    //config measure range
    Button measureMinPlusButton;
    Button measureMinMinusButton;
    TextField measureMinTextField;

    Button measureMaxPlusButton;
    Button measureMaxMinusButton;
    TextField measureMaxTextField;

    //insert notes
    TextArea lampNoteTextArea;
    TextArea measureNoteTextArea;
    Button lampNoteButton;
    Button measureNoteButton;

    //control circles
    Circle chipControl;
    Circle spectroControl;

    //info labels
    Label showActualAngle;
    Label showStepsLeft;


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
        scene = new Scene(mainPane, 1366, 768);
        mainPane.setPrefSize(1366,768);
        primaryStage.setTitle("Brewster");
        primaryStage.setScene(scene);
    }

    private void setLineChart(){
        scene.getStylesheets().add("gui/chart/style.css");
        lineChart = chart.getComponent();
        mainPane.setCenter(lineChart);
    }

    private void setTopPanel(){
        Font labelFont = new Font("Roboto",16.0);
        Insets labelPadding = new Insets(0,16,8,16);
        topPanel = new HBox();

        //logo
//        Image logo = new Image("logo1.png");
//        ImageView imageView1 = new ImageView(logo);
//        topPanel.getChildren().add(imageView1);

        //set modes
        VBox modeVBox = new VBox();
        GridPane modeGrid = new GridPane();
        GridPane measureCountGrid = new GridPane();


        modeButtonsGroup = new ToggleGroup();
        currentModeButton = new RadioButton("CURRENT");
        currentModeButton.setSelected(true);
        ltAvgModeButton = new RadioButton("LONG TIME AVG");
        measureCountPlusButton = new Button("+");
        measureCountMinusButton = new Button("-");
        measureCountTextField = new TextField();

        currentModeButton.setToggleGroup(modeButtonsGroup);
        ltAvgModeButton.setToggleGroup(modeButtonsGroup);

        Label modeLabel = new Label("set mode");
        modeLabel.getStyleClass().add("label");
        modeLabel.setPadding(labelPadding);
        modeLabel.setFont(labelFont);


        Label measureCountLabel = new Label("count of measures:");
        measureCountLabel.getStyleClass().add("label");

        modeGrid.add(modeLabel,0,0,2,1);
        modeGrid.add(currentModeButton,0,1,1,1);
        modeGrid.add(ltAvgModeButton,0,2,1,1);

        measureCountGrid.add(measureCountLabel,0,0,3,1);
        measureCountGrid.add(measureCountMinusButton,0,1,1,1);
        measureCountGrid.add(measureCountTextField,1,1,1,1);
        measureCountGrid.add(measureCountPlusButton,2,1,1,1);
        measureCountTextField.setPrefWidth(64);

        modeGrid.setHgap(5.0);
        modeGrid.setVgap(5.0);
        measureCountGrid.setHgap(5.0);
        measureCountGrid.setVgap(5.0);
        modeVBox.getChildren().add(modeGrid);
        modeVBox.getChildren().add(measureCountGrid);
        topPanel.getChildren().add(modeVBox);

        //set wavelength range
        GridPane waveGrid = new GridPane();
        waveBottomPlusButton =  new Button("+");
        waveBottomMinusButton = new Button("-");
        waveBottomTextField = new TextField();

        waveTopPlusButton =  new Button("+");
        waveTopMinusButton = new Button("-");
        waveTopTextField = new TextField();

        Label waveRangeLabel = new Label("wavelenght range");
        waveRangeLabel.getStyleClass().add("label");
        waveRangeLabel.setPadding(labelPadding);
        waveRangeLabel.setFont(labelFont);

        Label waveBottomLabel = new Label("FROM ");
        waveBottomLabel.getStyleClass().add("label");

        Label waveTopLabel = new Label("TO ");
        waveTopLabel.getStyleClass().add("label");

        waveGrid.add(waveRangeLabel,0,0,4,1);

        waveGrid.add(waveBottomLabel,0,1,1,1);
        waveGrid.add(waveBottomMinusButton,1,1,1,1);
        waveGrid.add(waveBottomTextField,2,1,1,1);
        waveGrid.add(waveBottomPlusButton,3,1,1,1);
        waveBottomTextField.setPrefWidth(64);

        waveGrid.add(waveTopLabel,0,2,1,1);
        waveGrid.add(waveTopMinusButton,1,2,1,1);
        waveGrid.add(waveTopTextField,2,2,1,1);
        waveGrid.add(waveTopPlusButton,3,2,1,1);
        waveTopTextField.setPrefWidth(64);

        waveGrid.setHgap(5.0);
        waveGrid.setVgap(5.0);
        topPanel.getChildren().add(waveGrid);

        //noise
        GridPane noiseGrid = new GridPane();
        measureNoiseButton =  new Button("MEASURE NOISE");
        applyNoiseButton = new CheckBox("APPLY NOISE");

        Label noiseLabel = new Label("noise");
        noiseLabel.getStyleClass().add("label");
        noiseLabel.setPadding(labelPadding);
        noiseLabel.setFont(labelFont);

        noiseGrid.add(noiseLabel,0,0,1,1);
        noiseGrid.add(measureNoiseButton,0,1,1,1);
        noiseGrid.add(applyNoiseButton,0,2,1,1);

        noiseGrid.setHgap(5.0);
        noiseGrid.setVgap(5.0);
        topPanel.getChildren().add(noiseGrid);

        //set measure range
        GridPane measureRangeGrid = new GridPane();
        measureMinPlusButton =  new Button("+");
        measureMinMinusButton = new Button("-");
        measureMinTextField = new TextField();

        measureMaxPlusButton =  new Button("+");
        measureMaxMinusButton = new Button("-");
        measureMaxTextField = new TextField();

        Label measureRangeLabel = new Label("measure range");
        measureRangeLabel.getStyleClass().add("label");
        measureRangeLabel.setPadding(labelPadding);
        measureRangeLabel.setFont(labelFont);

        Label measureMinLabel = new Label("MIN ");
        measureMinLabel.getStyleClass().add("label");

        Label measureMaxLabel = new Label("MAX ");
        measureMaxLabel.getStyleClass().add("label");

        measureRangeGrid.add(measureRangeLabel,0,0,4,1);

        measureRangeGrid.add(measureMinLabel,0,1,1,1);
        measureRangeGrid.add(measureMinMinusButton,1,1,1,1);
        measureRangeGrid.add(measureMinTextField,2,1,1,1);
        measureRangeGrid.add(measureMinPlusButton,3,1,1,1);
        measureMinTextField.setPrefWidth(64);

        measureRangeGrid.add(measureMaxLabel,0,2,1,1);
        measureRangeGrid.add(measureMaxMinusButton,1,2,1,1);
        measureRangeGrid.add(measureMaxTextField,2,2,1,1);
        measureRangeGrid.add(measureMaxPlusButton,3,2,1,1);
        measureMaxTextField.setPrefWidth(64);

        measureRangeGrid.setHgap(5.0);
        measureRangeGrid.setVgap(5.0);
        topPanel.getChildren().add(measureRangeGrid);

        //notes
        GridPane noteGrid = new GridPane();
        lampNoteTextArea = new TextArea();
        measureNoteTextArea = new TextArea();

        lampNoteButton =  new Button("SET");
        measureNoteButton =  new Button("SET");

        Label noteLabel = new Label("notes");
        noteLabel.getStyleClass().add("label");
        noteLabel.setPadding(labelPadding);
        noteLabel.setFont(labelFont);

        Label lampNoteLabel = new Label("lamp params ");
        lampNoteLabel.getStyleClass().add("label");

        Label measureNoteLabel = new Label("measure note ");
        measureNoteLabel.getStyleClass().add("label");

        noteGrid .add(noteLabel,0,0,3,1);

        noteGrid.add(lampNoteLabel,0,1,1,1);
        noteGrid.add(lampNoteTextArea,1,1,1,1);
        noteGrid.add(lampNoteButton,2,1,1,1);
        lampNoteTextArea.setPrefWidth(200);
        lampNoteTextArea.setPrefHeight(48);

        noteGrid.add(measureNoteLabel,0,2,1,1);
        noteGrid.add(measureNoteTextArea,1,2,1,1);
        noteGrid.add(measureNoteButton,2,2,1,1);
        measureNoteTextArea.setPrefWidth(200);
        measureNoteTextArea.setPrefHeight(48);

        noteGrid.setHgap(5.0);
        noteGrid.setVgap(5.0);
        topPanel.getChildren().add(noteGrid);

        //connecting controlers
        GridPane controlGrid = new GridPane();
        chipControl = new Circle();
        chipControl.setRadius(8.0);
        chipControl.setFill(Color.RED);
        chipControl.setStrokeWidth(1.0);

        spectroControl = new Circle();
        spectroControl.setRadius(8.0);
        spectroControl.setFill(Color.RED);
        spectroControl.setStrokeWidth(1.0);

        Label chipControlLabel = new Label("chip ");
        chipControlLabel.getStyleClass().add("label");
        chipControlLabel.setFont(labelFont);

        Label spectroControlLabel = new Label("spec ");
        spectroControlLabel.getStyleClass().add("label");
        spectroControlLabel.setFont(labelFont);

        controlGrid.add(chipControlLabel,0,0,1,1);
        controlGrid.add(chipControl,1,0,1,1);
        controlGrid.add(spectroControlLabel,0,1,1,1);
        controlGrid.add(spectroControl,1,1,1,1);

        controlGrid.setHgap(5.0);
        controlGrid.setVgap(5.0);
        controlGrid.setPadding(labelPadding);
        topPanel.getChildren().add(controlGrid);


        //add top panel in mainpane
        topPanel.getStyleClass().add("toppane");
        topPanel.setSpacing(24);


        Pane topAndInfoPanel = new Pane();
        setInfoPanel();
        topAndInfoPanel.getChildren().addAll(topPanel,infoPanel);
        topAndInfoPanel.setPrefHeight(250);
        mainPane.setTop(topAndInfoPanel);
        Insets margin = new javafx.geometry.Insets(16.0,0.0,0.0,250.0);
        mainPane.setMargin(topAndInfoPanel,margin);
    }

    private void setInfoPanel() {
        infoPanel = new HBox();
        Font labelFont = new Font("Roboto",16.0);
        Insets labelPadding = new Insets(0,16,8,16);

        //show actual angle
        GridPane actualGrid = new GridPane();
        GridPane stepsGrid = new GridPane();
        showActualAngle = new Label("UNKNOWN");
        showActualAngle.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        showActualAngle.setPadding(labelPadding);

        showStepsLeft = new Label("UNKNOWN");
        showStepsLeft.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        showStepsLeft.setPadding(labelPadding);

        Label actualAngleLabel = new Label("ACTUAL ANGLE");
        actualAngleLabel.getStyleClass().add("label");
        actualAngleLabel.setFont(labelFont);

        Label stepsLeftLabel = new Label("REMAINING STEPS");
        stepsLeftLabel.getStyleClass().add("label");
        stepsLeftLabel.setFont(labelFont);

        actualGrid.add(actualAngleLabel,0,0,1,1);
        actualGrid.add(showActualAngle,0,1,1,1);
        actualGrid.setHgap(5);
        actualGrid.setVgap(5);
        actualGrid.setAlignment(Pos.CENTER);

        stepsGrid.add(stepsLeftLabel,0,0,1,1);
        stepsGrid.add(showStepsLeft,0,1,1,1);
        stepsGrid.setHgap(5);
        stepsGrid.setVgap(5);
        stepsGrid.setAlignment(Pos.CENTER);

        infoPanel.getChildren().add(actualGrid);
        infoPanel.getChildren().add(stepsGrid);

        //set to main pane
        infoPanel.setSpacing(150);
        infoPanel.getStyleClass().add("infopane");
        infoPanel.setTranslateX(500);
        infoPanel.setTranslateY(200);
    }

    private void setLeftPanel(){
        leftPanel = new VBox();


        //arrows
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
