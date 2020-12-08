package gui;

import Exceptions.FilesAndFoldersExcetpions.WrongParameterException;
import Exceptions.SerialCommunicationExceptions.PicaxeConnectionErrorException;
import Exceptions.SerialCommunicationExceptions.PortNotFoundException;
import Exceptions.SerialCommunicationExceptions.UnknownCurrentAngleException;
import Exceptions.SpectrometerExceptions.SpectrometerNotConnected;
import gui.chart.Chart;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.util.Duration;
import measurement.MeasurementManager;
import serialCommunication.StepperMotor;
import settings.Settings;

import java.io.IOException;


public class GUI {


    private final Chart chart; //vykreslovanie grafu
    private final StepperMotor stepperMotor; //pre priame ovladanie motora
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
            "3 ms", "5 ms", "10 ms", "20 ms", "50 ms", "100 ms", "200 ms", "500 ms", "1 s", "2 s", "5 s", "10 s", "20 s", "30 s", "50 s");
    int[] expositionTimeValues = {3000, 5000, 10000, 20000, 50000, 100000, 200000, 500000, 1000000, 2000000, 5000000, 10000000, 20000000, 30000000, 50000000};
    ComboBox<String> comboBoxForExpositionTime;

    ObservableList<String> optionsForSerialPorts = FXCollections.observableArrayList("COM1", "COM2", "COM3", "COM4");
    ComboBox<String> comboBoxForSerialPorts;

    Button startButton;
    Button stopButton;

    ToggleGroup angleUnitsButtonsGroup;
    RadioButton gradiansButton;
    RadioButton degreesButton;

    //calibration
    Button confirmStartAngleForCalibrationButton;
    Button confirmStopAngleForCalibrationButton;
    TextField startAngleForCalibrationTextField;
    TextField stopAngleForCalibrationTextField;

    //measure mode
    ToggleGroup modeButtonsGroup;
    RadioButton currentModeButton;
    RadioButton ltAvgModeButton;
    TextField measureCountTextField;
    Button tryAverageButton;
    Label measureCountLabel;

    //config wavelength range
    TextField waveBottomTextField;
    TextField waveTopTextField;

    //noise
    Button measureNoiseButton;
    CheckBox applyNoiseButton;

    //config measure range
    TextField measureMinTextField;
    TextField measureMaxTextField;

    //insert notes
    TextArea lampNoteTextArea;
    TextArea measureNoteTextArea;

    //control circles
    Circle chipControl;
    Circle spectroControl;

    //info labels
    Label showActualAngle;
    Label showStepsLeft;
    Label showThetaAngle;

    //alert - na vypianie chybnych vstupov
    Alert alert;

    //premenne
    Integer numberOfPulses;

    Integer expositionTime;

    String angleUnits;

    String startAngleValueForCalibration;

    String stopAngleValueForCalibration;

    Boolean isAverageMode;

    String numberOfScansToAverage;

    String minWaveLengthToSave;

    String maxWaveLengthToSave;

    String measurementMinAngle;

    String measurementMaxAngle;

    String lampParameters;

    String comment;

    Boolean subtractBackground;

    Timeline startUpControlTimeline;

    boolean motorIsConnected = false;
    boolean spectrometerIsConnected = false;

    private CurrentAngleObserver currentAngleObserver;

    public GUI(Stage primaryStage, Chart chart, StepperMotor stepperMotor, MeasurementManager measurementManager) {
        this.chart = chart;
        this.stepperMotor = stepperMotor;
        this.measurementManager = measurementManager;
        this.primaryStage = primaryStage;

        setGuiComponents();
        setLineChart();
        setTopPanel();
        setLeftPanel();

        setFields();

        this.primaryStage.show();
        handlingLeftPanel(); //tlacidla a textboxy laveho panelu
        handlingTopPanel();

        disableButtons(true);
        controlExternalDevicesAtProgramStartUp();
    }

    /**
     * kvoli tomu ako je implementovany picaxe, tak to musi byt takto
     */
    private void controlExternalDevicesAtProgramStartUp() {
        motorIsConnected = false;
        spectrometerIsConnected = false;
        startUpControlTimeline = new Timeline(new KeyFrame(Duration.millis(3000), e -> {
            try {

                if (stepperMotor.checkPicaxeConnection()) {
                    chipControl.setFill(Color.GREEN);
                    motorIsConnected = true;
                } else {
                    chipControl.setFill(Color.RED);
                }

                measurementManager.checkConnectionOfSpectrometer();
                spectroControl.setFill(Color.GREEN);
                spectrometerIsConnected = true;

                if (motorIsConnected && spectrometerIsConnected) {
                    disableButtons(false);
                    startLiveMode(measurementManager); //mozes spustit livemode
                    startUpControlTimeline.stop(); //ak sa to dostalo sem, tak mozes vypnut toto kontrolovanie
                }
            } catch (SpectrometerNotConnected ex) {
                spectroControl.setFill(Color.RED);
            }
        }));
        startUpControlTimeline.setCycleCount(Timeline.INDEFINITE);
        startUpControlTimeline.play();
    }


    private void startLiveMode(MeasurementManager measurementManager) {
        try {
            measurementManager.checkConnectionOfSpectrometer();
            measurementManager.startLiveMode(expositionTime, chart);
        } catch (SpectrometerNotConnected ex) {
            StringBuilder sb = new StringBuilder(ex.getMessage());
            sb.append("\n");
            sb.append("Please, restart the application with connected spectrometer");
            showAlert("Spectrometer is not connected", sb.toString());
            disableButtons(true);
        }
    }

    private void setSettings() throws WrongParameterException {
        this.numberOfScansToAverage = measureCountTextField.getText();
        this.minWaveLengthToSave = waveBottomTextField.getText();
        this.maxWaveLengthToSave = waveTopTextField.getText();
        this.measurementMinAngle = measureMinTextField.getText();
        this.measurementMaxAngle = measureMaxTextField.getText();
        this.lampParameters = lampNoteTextArea.getText();
        this.comment = measureNoteTextArea.getText();

        Settings.getInstance().checkAndSetParameters(isAverageMode, numberOfScansToAverage, angleUnits, measurementMinAngle, measurementMaxAngle,
                lampParameters, subtractBackground, expositionTime, minWaveLengthToSave, maxWaveLengthToSave, comment, numberOfPulses);
    }

    private void disableButtons(boolean value) {
        buttonRIGHT.setDisable(value);
        buttonLEFT.setDisable(value);
        buttonUP.setDisable(value);
        buttonDOWN.setDisable(value);
        textFieldForPulses.setDisable(value);
        textFieldForMoveToAngle.setDisable(value);

        moveToAngleButton.setDisable(value);
        comboBoxForExpositionTime.setDisable(value);
        startButton.setDisable(value);
        gradiansButton.setDisable(value);
        degreesButton.setDisable(value);

        confirmStopAngleForCalibrationButton.setDisable(value);
        stopAngleForCalibrationTextField.setDisable(value);

        confirmStartAngleForCalibrationButton.setDisable(value);
        startAngleForCalibrationTextField.setDisable(value);

        currentModeButton.setDisable(value);
        ltAvgModeButton.setDisable(value);
        measureCountTextField.setDisable(value);

        waveBottomTextField.setDisable(value);
        waveTopTextField.setDisable(value);

        measureMinTextField.setDisable(value);
        measureMaxTextField.setDisable(value);

        lampNoteTextArea.setDisable(value);
        measureNoteTextArea.setDisable(value);

        measureNoiseButton.setDisable(value);
        applyNoiseButton.setDisable(value);


    }

    private void setGuiComponents() {
        mainPane = new BorderPane();
        scene = new Scene(mainPane, 1410, 800);
        mainPane.setPrefSize(1410, 800);
        primaryStage.setTitle("Brewster");
        primaryStage.setScene(scene);
    }

    private void setLineChart() {
        scene.getStylesheets().add("gui/chart/style.css");
        lineChart = chart.getComponent();
        mainPane.setCenter(lineChart);
    }

    private void setTopPanel() {
        Font labelFont = new Font("Roboto", 16.0);
        Insets labelPadding = new Insets(0, 16, 8, 16);
        topPanel = new HBox();

        //set modes
        VBox modeVBox = new VBox();
        GridPane modeGrid = new GridPane();
        GridPane measureCountGrid = new GridPane();


        modeButtonsGroup = new ToggleGroup();
        currentModeButton = new RadioButton("CURRENT");
        ltAvgModeButton = new RadioButton("LONG TIME AVG");
        measureCountTextField = new TextField();
        tryAverageButton = new Button("TRY IN LIVE MODE");


        currentModeButton.setToggleGroup(modeButtonsGroup);
        ltAvgModeButton.setToggleGroup(modeButtonsGroup);

        Label modeLabel = new Label("set mode");
        modeLabel.getStyleClass().add("label");
        modeLabel.setPadding(labelPadding);
        modeLabel.setFont(labelFont);

        measureCountLabel = new Label("count of measures:");
        measureCountLabel.getStyleClass().add("label");

        modeGrid.add(modeLabel, 0, 0, 2, 1);
        modeGrid.add(currentModeButton, 0, 1, 1, 1);
        modeGrid.add(ltAvgModeButton, 0, 2, 1, 1);
        measureCountGrid.add(measureCountLabel, 0, 0, 3, 1);
        measureCountGrid.add(measureCountTextField, 1, 1, 1, 1);
        measureCountGrid.add(tryAverageButton, 2, 1, 1, 1);
        measureCountTextField.setPrefWidth(50);

        measureCountLabel.setVisible(false);
        measureCountTextField.setVisible(false);
        tryAverageButton.setVisible(false);

        modeGrid.setHgap(5.0);
        modeGrid.setVgap(5.0);
        measureCountGrid.setHgap(5.0);
        measureCountGrid.setVgap(5.0);
        modeVBox.getChildren().add(modeGrid);
        modeVBox.getChildren().add(measureCountGrid);
        topPanel.getChildren().add(modeVBox);

        //set wavelength range
        GridPane waveGrid = new GridPane();
        waveBottomTextField = new TextField();
        waveTopTextField = new TextField();

        Label waveRangeLabel = new Label("wavelenght range");
        waveRangeLabel.getStyleClass().add("label");
        waveRangeLabel.setPadding(labelPadding);
        waveRangeLabel.setFont(labelFont);

        Label waveBottomLabel = new Label("FROM ");
        waveBottomLabel.getStyleClass().add("label");

        Label waveTopLabel = new Label("TO ");
        waveTopLabel.getStyleClass().add("label");

        waveGrid.add(waveRangeLabel, 0, 0, 4, 1);

        waveGrid.add(waveBottomLabel, 0, 1, 1, 1);
        waveGrid.add(waveBottomTextField, 2, 1, 1, 1);
        waveBottomTextField.setPrefWidth(100);

        waveGrid.add(waveTopLabel, 0, 2, 1, 1);
        waveGrid.add(waveTopTextField, 2, 2, 1, 1);
        waveTopTextField.setPrefWidth(100);

        waveGrid.setHgap(5.0);
        waveGrid.setVgap(5.0);
        topPanel.getChildren().add(waveGrid);

        //noise
        GridPane noiseGrid = new GridPane();
        measureNoiseButton = new Button("MEASURE NOISE");
        applyNoiseButton = new CheckBox("APPLY NOISE");

        Label noiseLabel = new Label("noise");
        noiseLabel.getStyleClass().add("label");
        noiseLabel.setPadding(labelPadding);
        noiseLabel.setFont(labelFont);

        noiseGrid.add(noiseLabel, 0, 0, 1, 1);
        noiseGrid.add(measureNoiseButton, 0, 1, 1, 1);
        noiseGrid.add(applyNoiseButton, 0, 2, 1, 1);

        noiseGrid.setHgap(5.0);
        noiseGrid.setVgap(5.0);
        topPanel.getChildren().add(noiseGrid);

        //set measure range
        GridPane measureRangeGrid = new GridPane();
        measureMinTextField = new TextField();
        measureMaxTextField = new TextField();

        Label measureRangeLabel = new Label("measure range");
        measureRangeLabel.getStyleClass().add("label");
        measureRangeLabel.setPadding(labelPadding);
        measureRangeLabel.setFont(labelFont);

        Label measureMinLabel = new Label("MIN ");
        measureMinLabel.getStyleClass().add("label");

        Label measureMaxLabel = new Label("MAX ");
        measureMaxLabel.getStyleClass().add("label");

        measureRangeGrid.add(measureRangeLabel, 0, 0, 4, 1);

        measureRangeGrid.add(measureMinLabel, 0, 1, 1, 1);
        measureRangeGrid.add(measureMinTextField, 2, 1, 1, 1);
        measureMinTextField.setPrefWidth(64);

        measureRangeGrid.add(measureMaxLabel, 0, 2, 1, 1);
        measureRangeGrid.add(measureMaxTextField, 2, 2, 1, 1);
        measureMaxTextField.setPrefWidth(100);

        measureRangeGrid.setHgap(5.0);
        measureRangeGrid.setVgap(5.0);
        topPanel.getChildren().add(measureRangeGrid);

        //notes
        GridPane noteGrid = new GridPane();
        lampNoteTextArea = new TextArea();
        measureNoteTextArea = new TextArea();

        Label noteLabel = new Label("notes");
        noteLabel.getStyleClass().add("label");
        noteLabel.setPadding(labelPadding);
        noteLabel.setFont(labelFont);

        Label lampNoteLabel = new Label("lamp params ");
        lampNoteLabel.getStyleClass().add("label");

        Label measureNoteLabel = new Label("measurement note ");
        measureNoteLabel.getStyleClass().add("label");

        noteGrid.add(noteLabel, 0, 0, 1, 1);

        noteGrid.add(lampNoteLabel, 0, 1, 1, 1);
        noteGrid.add(lampNoteTextArea, 0, 2, 1, 1);
        lampNoteTextArea.setPrefWidth(300);
        lampNoteTextArea.setPrefHeight(32);

        noteGrid.add(measureNoteLabel, 0, 3, 1, 1);
        noteGrid.add(measureNoteTextArea, 0, 4, 1, 1);
        measureNoteTextArea.setPrefWidth(300);
        measureNoteTextArea.setPrefHeight(32);

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

        controlGrid.add(chipControlLabel, 0, 0, 1, 1);
        controlGrid.add(chipControl, 1, 0, 1, 1);
        controlGrid.add(spectroControlLabel, 0, 1, 1, 1);
        controlGrid.add(spectroControl, 1, 1, 1, 1);


        Label choosePortLabel = new Label("Serial port for chip");
        choosePortLabel.getStyleClass().add("label");
        choosePortLabel.setFont(labelFont);

        comboBoxForSerialPorts = new ComboBox<>(optionsForSerialPorts);
        comboBoxForSerialPorts.setPrefWidth(100);

        controlGrid.add(choosePortLabel, 0, 3, 1, 1);
        controlGrid.add(comboBoxForSerialPorts, 0, 4, 1, 1);

        controlGrid.setHgap(5.0);
        controlGrid.setVgap(5.0);
        controlGrid.setPadding(labelPadding);
        topPanel.getChildren().add(controlGrid);


        //add top panel in mainpane
        topPanel.getStyleClass().add("toppane");
        topPanel.setSpacing(24);

        Pane topAndInfoPanel = new Pane();
        setInfoPanel();
        topAndInfoPanel.getChildren().addAll(topPanel, infoPanel);
        topAndInfoPanel.setPrefHeight(250);
        mainPane.setTop(topAndInfoPanel);
        Insets margin = new javafx.geometry.Insets(16.0, 0.0, 0.0, 150);
        mainPane.setMargin(topAndInfoPanel, margin);
    }

    private void setInfoPanel() {
        infoPanel = new HBox();
        Font labelFont = new Font("Roboto", 16.0);
        Insets labelPadding = new Insets(0, 16, 8, 16);

        //show actual angle
        GridPane actualGrid = new GridPane();
        GridPane stepsGrid = new GridPane();
        GridPane thetaGrid = new GridPane();

        showActualAngle = new Label("UNKNOWN");
        showActualAngle.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        showActualAngle.setPadding(labelPadding);

        showStepsLeft = new Label("UNKNOWN");
        showStepsLeft.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        showStepsLeft.setPadding(labelPadding);

        showThetaAngle = new Label("UNKNOWN");
        showThetaAngle.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        showThetaAngle.setPadding(labelPadding);

        Label actualAngleLabel = new Label("ACTUAL ANGLE");
        actualAngleLabel.getStyleClass().add("label");
        actualAngleLabel.setFont(labelFont);

        Label stepsLeftLabel = new Label("REMAINING STEPS");
        stepsLeftLabel.getStyleClass().add("label");
        stepsLeftLabel.setFont(labelFont);

        Label thetaLabel = new Label("THETA");
        thetaLabel.getStyleClass().add("label");
        thetaLabel.setFont(labelFont);

        thetaGrid.add(thetaLabel, 0, 0, 1, 1);
        thetaGrid.add(showThetaAngle, 0, 1, 1, 1);
        thetaGrid.setHgap(5);
        thetaGrid.setVgap(5);
        thetaGrid.setAlignment(Pos.CENTER);

        actualGrid.add(actualAngleLabel, 0, 0, 1, 1);
        actualGrid.add(showActualAngle, 0, 1, 1, 1);
        actualGrid.setHgap(5);
        actualGrid.setVgap(5);
        actualGrid.setAlignment(Pos.CENTER);

        stepsGrid.add(stepsLeftLabel, 0, 0, 1, 1);
        stepsGrid.add(showStepsLeft, 0, 1, 1, 1);
        stepsGrid.setHgap(5);
        stepsGrid.setVgap(5);
        stepsGrid.setAlignment(Pos.CENTER);

        infoPanel.getChildren().add(thetaGrid);
        infoPanel.getChildren().add(actualGrid);
        infoPanel.getChildren().add(stepsGrid);

        //set to main pane
        infoPanel.setSpacing(150);
        infoPanel.getStyleClass().add("infopane");
        infoPanel.setTranslateX(500);
        infoPanel.setTranslateY(200);
    }

    private void setLeftPanel() {
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


        moveToAngleGrid.add(label1, 0, 0, 1, 1);
        moveToAngleGrid.add(textFieldForMoveToAngle, 0, 1, 1, 1);
        moveToAngleGrid.add(moveToAngleButton, 1, 1, 1, 1);

        leftPanel.getChildren().add(moveToAngleGrid);

        //set exposition time
        GridPane setExpositionTimeGrid = new GridPane();

        Label label2 = new Label("Set exposition time");
        label2.setPrefWidth(200);
        label2.getStyleClass().add("label");

        comboBoxForExpositionTime = new ComboBox<>(optionsForExpositionTime);
        comboBoxForExpositionTime.setPrefWidth(80);

        setExpositionTimeGrid.add(label2, 0, 0, 1, 1);
        setExpositionTimeGrid.add(comboBoxForExpositionTime, 0, 1, 1, 1);

        leftPanel.getChildren().add(setExpositionTimeGrid);

        //start stop buttons
        GridPane startStopGrid = new GridPane();
        startButton = new Button("START");
        stopButton = new Button("STOP");
        startButton.setPrefWidth(100);
        stopButton.setPrefWidth(100);
        startButton.getStyleClass().add("startButton");
        stopButton.getStyleClass().add("stopButton");

        startStopGrid.add(startButton, 0, 0, 1, 1);
        startStopGrid.add(stopButton, 1, 0, 1, 1);

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
        angleUnitsGrid.add(label3, 0, 0, 1, 1);
        angleUnitsGrid.add(gradiansButton, 0, 1, 1, 1);
        angleUnitsGrid.add(degreesButton, 1, 1, 1, 1);

        leftPanel.getChildren().add(angleUnitsGrid);

        //calibration
        GridPane calibrationGrid = new GridPane();
        Label label4 = new Label("CALIBRATION");
        label4.getStyleClass().add("label");
        Label labelStart = new Label("START");
        labelStart.getStyleClass().add("label");
        Label labelStop = new Label("STOP");
        labelStop.getStyleClass().add("label");
        confirmStartAngleForCalibrationButton = new Button("CONFIRM START");
        confirmStopAngleForCalibrationButton = new Button("CONFIRM STOP ");
        startAngleForCalibrationTextField = new TextField();
        stopAngleForCalibrationTextField = new TextField();

        startAngleForCalibrationTextField.setPrefWidth(80);
        stopAngleForCalibrationTextField.setPrefWidth(80);

        calibrationGrid.add(label4, 0, 0, 1, 1);
        calibrationGrid.add(labelStart, 0, 1, 1, 1);
        calibrationGrid.add(startAngleForCalibrationTextField, 1, 1, 1, 1);
        calibrationGrid.add(confirmStartAngleForCalibrationButton, 2, 1, 1, 1);

        calibrationGrid.add(labelStop, 0, 2, 1, 1);
        calibrationGrid.add(stopAngleForCalibrationTextField, 1, 2, 1, 1);
        calibrationGrid.add(confirmStopAngleForCalibrationButton, 2, 2, 1, 1);

        leftPanel.getChildren().add(calibrationGrid);

        //set to main pane
        leftPanel.setSpacing(20);
        leftPanel.getStyleClass().add("leftpane");
        mainPane.setLeft(leftPanel);
    }

    private void handlingLeftPanel() {
        handlingArrowsButtons();
        handlingExpositionTimeComboBox();
        handlingMoveToAngleButton();
        handlingStartAndStopButtons();
        handlingSettingAngleUnitsRadioButtons();
        handlingCalibration();

    }

    private void handlingArrowsButtons() {
        buttonUP.setOnAction(e -> {
            numberOfPulses++;
            try {
                Settings.getInstance().setStepSize(numberOfPulses);
            } catch (WrongParameterException wrongParameterException) {
                wrongParameterException.printStackTrace();
            }
            textFieldForPulses.setText("" + numberOfPulses);
        });

        buttonDOWN.setOnAction(e -> {
            numberOfPulses--;
            if (numberOfPulses < 1) {
                numberOfPulses = 1;
            }
            try {
                Settings.getInstance().setStepSize(numberOfPulses);
            } catch (WrongParameterException wrongParameterException) {
                wrongParameterException.printStackTrace();
            }
            textFieldForPulses.setText("" + numberOfPulses);
        });

        buttonLEFT.setOnAction(e -> {
            stepperMotor.stepBackwards();
            Settings.getInstance().stepsSinceCalibrationStart++;
        });

        buttonRIGHT.setOnAction(e -> {
            stepperMotor.stepForward();
            Settings.getInstance().stepsSinceCalibrationStart++;
        });
    }

    private void handlingExpositionTimeComboBox() {
        comboBoxForExpositionTime.setOnAction(e -> {
            int index = getIndexFromComboBox(comboBoxForExpositionTime.getValue());
            expositionTime = expositionTimeValues[index];
            measurementManager.stopLiveMode();
            measurementManager.startLiveMode(expositionTime, chart);
        });

    }

    private void handlingMoveToAngleButton() {
        moveToAngleButton.setOnAction(e -> {
            String value = textFieldForMoveToAngle.getText();
            try {
                stepperMotor.moveToAngle(value);
            } catch (UnknownCurrentAngleException | WrongParameterException ex) {
                showAlert("Move to angle error", ex.getMessage());
            }
        });
    }

    private void handlingStartAndStopButtons() {
        stopButton.setOnAction(e -> {
            Platform.exit();
        });

        startButton.setOnAction(e -> {
            try {
                setSettings();
                measurementManager.stopLiveMode();
                measurementManager.startSeriesOfMeasurements(chart, showStepsLeft);
            } catch (WrongParameterException ex) {
                showAlert("WrongParameters", ex.getMessage());
            } catch (SpectrometerNotConnected ex) {
                spectroControl.setFill(Color.RED);
                showAlert("Spectrometer not connected", ex.getMessage());
            } catch (PicaxeConnectionErrorException ex) {
                chipControl.setFill(Color.RED);
                showAlert("Picaxe not connected", ex.getMessage());
            }
        });
    }

    private void handlingSettingAngleUnitsRadioButtons() {
        gradiansButton.setOnAction(e -> {
            angleUnits = "gradians";
        });
        degreesButton.setOnAction(e -> {
            angleUnits = "degrees";
        });
    }

    private void handlingCalibration() {
        confirmStartAngleForCalibrationButton.setOnAction(e -> {
            startAngleValueForCalibration = startAngleForCalibrationTextField.getText();
            try {
                Settings.getInstance().setCalibrationMinAngle(startAngleValueForCalibration);
                System.out.println(startAngleValueForCalibration);
            } catch (WrongParameterException ex) {
                System.out.println(ex.getMessage());
                startAngleValueForCalibration = "";
                showAlert("Wrong input for calibration", ex.getMessage());
            }
        });

        confirmStopAngleForCalibrationButton.setOnAction(e -> {
            stopAngleValueForCalibration = stopAngleForCalibrationTextField.getText();
            try {
                Settings.getInstance().setCalibrationMaxAngle(stopAngleValueForCalibration);
//                System.out.println(stopAngleValueForCalibration);
                System.out.println("vysledok po kalibracia" + Settings.getInstance().getPulseToAngleRatio());
                if (currentAngleObserver == null) {
                    currentAngleObserver = new CurrentAngleObserver(stepperMotor, showActualAngle, showThetaAngle);
                    stepperMotor.attach(currentAngleObserver);
                    stepperMotor.currentAngle = Settings.getInstance().getCalibrationMaxAngle();
                    currentAngleObserver.update();
                }
            } catch (WrongParameterException ex) {
                System.out.println(ex.getMessage());
                stopAngleValueForCalibration = "";
                showAlert("Wrong input for calibration", ex.getMessage());
            }
        });
    }

    private void handlingTopPanel() {
        handlingModeRadioButtons();
        handlingTryAverageButton();
        handlingNoise();
        handlingNoiseButton();
        handlingComboboxForSerialPorts();
    }

    private void handlingTryAverageButton(){
        tryAverageButton.setOnAction(e -> {
            String value = measureCountTextField.getText();
            try{
                measurementManager.stopLiveMode();
                Settings.getInstance().setNumberOfScansToAverage(value);
            } catch (WrongParameterException ex) {
                showAlert("AVG MODE", ex.getMessage());
            } finally {
                measurementManager.startLiveMode(expositionTime, chart);
            }
        });
    }

    private void handlingNoiseButton() {
        measureNoiseButton.setOnAction(e -> {
            measurementManager.stopLiveMode();
            measurementManager.measureBackground();
            disableButtons(true);
            Timeline tmp = new Timeline(new KeyFrame(Duration.millis((expositionTime / 1000) + 50), e2 -> {
            }));
            tmp.setCycleCount(1);
            tmp.play();
            tmp.setOnFinished(e2 -> {
                double[] backgrnd = Settings.getInstance().getBackground();
                disableButtons(false);
                measurementManager.startLiveMode(expositionTime, chart);
            });
        });
    }

    private void handlingModeRadioButtons() {
        currentModeButton.setOnAction(e -> {
            measurementManager.stopLiveMode();
            this.isAverageMode = false;
            this.numberOfScansToAverage = "1";
            try {
                Settings.getInstance().setNumberOfScansToAverage(numberOfScansToAverage);
            } catch (WrongParameterException wrongParameterException) {
                System.out.println(wrongParameterException.getMessage());
            }
            avgMeasureSectionToggle(false);
            measurementManager.startLiveMode(expositionTime, chart);
        });

        ltAvgModeButton.setOnAction(e -> {

            this.isAverageMode = true;
            this.numberOfScansToAverage = "2";
            avgMeasureSectionToggle(true);
        });
    }


    private void avgMeasureSectionToggle(Boolean isAVG) {
        if (isAVG) {
            measureCountLabel.setVisible(true);
            measureCountTextField.setVisible(true);
            measureCountTextField.setText("2");
            tryAverageButton.setVisible(true);
        } else {
            measureCountLabel.setVisible(false);
            measureCountTextField.setVisible(false);
            tryAverageButton.setVisible(false);
        }
    }

    private void handlingNoise() {
        applyNoiseButton.setOnAction(e -> {
            if (applyNoiseButton.isSelected()) {
                if (Settings.getInstance().getBackground() == null) {
                    applyNoiseButton.setSelected(false);
                    showAlert("missingBackground", "You have no measured NOISE BACKGROUND");
                } else {
                    System.out.println("noise subtract applied");
                    subtractBackground = true;
                }

            } else {
                System.out.println("noise subtract not applied");
                subtractBackground = false;
            }
        });
    }


    private void handlingComboboxForSerialPorts() {

        comboBoxForSerialPorts.setOnAction(e -> {
            String serialPortName = comboBoxForSerialPorts.getValue();
            try {
                stepperMotor.findPicaxe(serialPortName);
            } catch (PortNotFoundException portNotFoundException) {
                portNotFoundException.printStackTrace(); // nenasli sa ziadne porty
            }
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(200), event -> { }));
            timeline.setCycleCount(10); // 10 * 200ms = 2sec
            timeline.setOnFinished(finish -> {
                if (stepperMotor.checkPicaxeConnection()) { /* picaxe sa nasiel a je otvoreny ( pripraveny na komunikaciu ) */ }
                else { /* picaxe sa nenasiel ( serialPort je null alebo nepripraveny na komunikaciu ) */ }
            });
            timeline.play();
            //TODO zafarbenie gulicky + odomknutie start tlacidla
        });
    }

    private void setFields() {

        this.numberOfPulses = 1;
        this.textFieldForPulses.setText("" + numberOfPulses);

        this.comboBoxForExpositionTime.setValue(optionsForExpositionTime.get(0));
        this.expositionTime = expositionTimeValues[0];

        this.angleUnits = "degrees";
        this.degreesButton.setSelected(true);

        this.startAngleValueForCalibration = "";
        this.stopAngleValueForCalibration = "";

        this.isAverageMode = false;
        this.currentModeButton.setSelected(true);
        this.numberOfScansToAverage = "1";

        this.minWaveLengthToSave = null;
        this.maxWaveLengthToSave = null;

        this.measurementMinAngle = null;
        this.measurementMaxAngle = null;

        this.lampParameters = null;
        this.comment = null;

        this.applyNoiseButton.setSelected(false);
        this.subtractBackground = false;

        this.comboBoxForSerialPorts.setValue("-");

        this.alert = new Alert(Alert.AlertType.WARNING);
    }

    private int getIndexFromComboBox(String key) {
        for (int i = 0; i < optionsForExpositionTime.size(); i++) {
            if (optionsForExpositionTime.get(i).equals(key)) {
                return i;
            }
        }
        return -999;
    }

    private void showAlert(String headerText, String errorMesage) {
        alert.setHeaderText(headerText);
        alert.setContentText(errorMesage);
        alert.show();
    }
}
