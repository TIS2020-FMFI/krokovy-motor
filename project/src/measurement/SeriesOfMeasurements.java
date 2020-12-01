package measurement;

import Exceptions.FilesAndFoldersExcetpions.*;
import Exceptions.SerialCommunicationExceptions.PicaxeConnectionErrorException;
import Exceptions.SpectrometerExceptions.SpectrometerNotConnected;
import com.oceanoptics.omnidriver.api.wrapper.Wrapper;
import gui.chart.Chart;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;
import serialCommunication.Spectrometer;
import serialCommunication.StepperMotor;
import settings.Settings;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SeriesOfMeasurements {

    List<Measurement> measurements = new ArrayList();
    String mainDirPath = "measuredData";
    private Timeline seriesOfMTimeline;

    private int remainingSteps;
    private Wrapper wrapper;
    private StepperMotor stepperMotor;
    private Spectrometer spectrometer;
    private MeasurementManager measurementManager;

    public SeriesOfMeasurements(Wrapper wrapper, StepperMotor stepperMotor, Spectrometer spectrometer,
                                MeasurementManager measurementManager) {
        this.wrapper = wrapper;
        this.stepperMotor = stepperMotor;
        this.spectrometer = spectrometer;
        this.measurementManager = measurementManager;
    }

    public SeriesOfMeasurements()  { }

    public void begin(Chart chart, Label currentAngleLabel, Label remainingStepsLabel) throws PicaxeConnectionErrorException, SpectrometerNotConnected {
        if(stepperMotor.checkPicaxeConnection() == false){
            throw new PicaxeConnectionErrorException("Picaxe is not connected");
        }
        spectrometer.checkConnection();
        moveAndStartSeries(chart, currentAngleLabel, remainingStepsLabel);
    }

    private void moveAndStartSeries(Chart chart, Label currentAngleLabel, Label remainingStepsLabel){
        double angle = Settings.getInstance().getMeasurementMinAngle();
        Timeline moving;
        if (stepperMotor.currentAngle < angle) {
            moving = new Timeline(new KeyFrame(Duration.millis(stepperMotor.getImpulseTime()), e -> {
                stepperMotor.moveOnePulseForward(currentAngleLabel);
            }));
        } else {
            moving = new Timeline(new KeyFrame(Duration.millis(stepperMotor.getImpulseTime()), e -> {
                stepperMotor.moveOnePulseBackwards(currentAngleLabel);
            }));
        }
        moving.setCycleCount(stepperMotor.pulsesNeededToMove(angle));
        moving.setOnFinished(e -> startSeries(chart, currentAngleLabel, remainingStepsLabel));
        moving.play();
    }

    private void startSeries(Chart chart, Label currentAngleLabel, Label remainingStepsLabel) {
        Double interval = (Settings.getInstance().getIntegrationTime()/1000) * Settings.getInstance().getNumberOfScansToAverage()
                            + chart.getDrawingTime() + stepperMotor.getStepTime();
        Double startAngle = Settings.getInstance().getMeasurementMinAngle();
        Double endAngle = Settings.getInstance().getMeasurementMaxAngle();

        Integer stepsToDo = stepperMotor.stepsNeededToMove(endAngle);
        remainingSteps = stepsToDo;
        remainingStepsLabel.setText(String.valueOf(remainingSteps));

        double[] wavelengths = wrapper.getWavelengths(0);
        chart.setxValues(wavelengths);
        wrapper.setIntegrationTime(0, Settings.getInstance().getIntegrationTime());
        wrapper.setScansToAverage(0, Settings.getInstance().getNumberOfScansToAverage());

        seriesOfMTimeline = new Timeline(new KeyFrame(Duration.millis(interval), e -> {

            measureAndVisualize(chart, wavelengths);

            if (startAngle < endAngle){
                stepperMotor.stepForward(currentAngleLabel);
            } else {
                stepperMotor.stepBackwards(currentAngleLabel);
            }
            remainingSteps --;
            remainingStepsLabel.setText(String.valueOf(remainingSteps));
        }));

        seriesOfMTimeline.setCycleCount(stepsToDo);
        seriesOfMTimeline.play();
        seriesOfMTimeline.setOnFinished(e -> {
            try {
                measureAndVisualize(chart, wavelengths); //odmeriam aj na konci intervalu
                save();
                measurementManager.startLiveMode(Settings.getInstance().getIntegrationTime(), chart);
            } catch (ParameterIsNullException parameterIsNullException) {
                parameterIsNullException.printStackTrace();
            }
        });
    }

    private void measureAndVisualize(Chart chart, double[] wavelengths){
        double[] spectralData = wrapper.getSpectrum(0);
        substractBackgroundIfNeeded(spectralData);
        chart.replaceMainData(spectralData, "last measured data");
        try {
            addMeasurement(new Measurement(spectralData, wavelengths, stepperMotor.currentAngle));
        } catch (ParameterIsNullException parameterIsNullException) {
            parameterIsNullException.printStackTrace();
        }
    }

    private void substractBackgroundIfNeeded(double[] values){
        double[] background = Settings.getInstance().getBackground();
        if (Settings.getInstance().getSubtractBackground() == false || background == null){
            return;
        }
        for (int i = 0; i < Math.min(values.length, background.length); i++) {
            values[i] = Math.max(values[i] - background[i], 0);
        }
    }

    public void stop(){
        seriesOfMTimeline.stop();
    }

    public void save() throws ParameterIsNullException {
        if(measurements.isEmpty()) throw new ParameterIsNullException("there are no measurements to save");

        //create dir for this series
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date(System.currentTimeMillis());
        String seriesDirName = formatter.format(date);

        String seriesDirPath = mainDirPath + File.separator + seriesDirName;
        File seriesDir = new File(seriesDirPath);
        seriesDir.mkdirs();  //ak mainDir neexistuje, mkdirs() vytvori aj to

        //save config file to the created dir
        try {
            Settings.getInstance().saveToFile(seriesDirPath);
        } catch (FileAlreadyExistsException e) {
            e.printStackTrace();
        } catch (MissingFolderException e) {
            e.printStackTrace();
        } catch (FileDoesNotExistException e) {
            e.printStackTrace();
        }

        //save measurements to the created dir
        for(Measurement m : measurements){
            try {
                m.saveToFile(seriesDirPath);
            } catch (MissingFolderException e) {
                e.printStackTrace();
            } catch (FileAlreadyExistsException e) {
                e.printStackTrace();
            } catch (FileDoesNotExistException e) {
                e.printStackTrace();
            }
        }
    }

    public void addMeasurement(Measurement m) throws ParameterIsNullException {
        if(m == null) throw new ParameterIsNullException("measurement cannot be null");
        measurements.add(m);
    }


    public void setMainDirPath(String mainDirPath) {
        this.mainDirPath = mainDirPath;
    }



//    public static void main(String[] args) {  //test
//        double[] waveLengths = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
//        double[] values = {100.78, 150.01, 200.8, 300.0, 50.5, 300, 222.2, 134.12, 123.10, 99.99};
//        double angle = 90.123457;
//        Measurement m1 = null;
//        double angle2 = 90.12;
//        Measurement m2 = null;
//        try {
//            m1 = new Measurement(values, waveLengths, angle);
//            m2 = new Measurement(values, waveLengths, angle2);
//            SeriesOfMeasurements series = new SeriesOfMeasurements();
//            series.addMeasurement(m1);
//            series.addMeasurement(m2);
//            /*ConfigurationFile c = new ConfigurationFile(true, 10, "gradians", 0d,
//                    120d, "wolframova halogenova lampa, 10 voltov, 10 amperov, velmi dobra lampa", false,
//                    50, 200, 400, 0.5, "koment ku meraniu");
//            series.setConfigurationFile(c);*/
//            Settings.setStepToAngleRatio(1.0);
//            series.save();
//        } catch (ParameterIsNullException e) {
//            System.out.println(e.getMessage());
//        }
//    }
}
