package measurement;

import Exceptions.FilesAndFoldersExcetpions.*;
import Exceptions.SerialCommunicationExceptions.PicaxeConnectionErrorException;
import Exceptions.SpectrometerExceptions.SpectrometerNotConnected;
import Interfaces.Observer;
import Interfaces.Subject;
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
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SeriesOfMeasurements implements Subject {

    FittedMinimum fittedMinimum;

    List<Measurement> measurements = new ArrayList();
    String mainDirPath = "measuredData";
    private Timeline seriesOfMTimeline;
    private Timeline waitForMeasuring;
    private Timeline waitForStep;
    private Timeline waitForLastIteration;

    public int remainingSteps = 0;
    private Wrapper wrapper;
    private StepperMotor stepperMotor;
    private Spectrometer spectrometer;
    private MeasurementManager measurementManager;
    private ArrayList<Observer> observers = new ArrayList();

    public SeriesOfMeasurements(Wrapper wrapper, StepperMotor stepperMotor, Spectrometer spectrometer,
                                MeasurementManager measurementManager) {
        this.wrapper = wrapper;
        this.stepperMotor = stepperMotor;
        this.spectrometer = spectrometer;
        this.measurementManager = measurementManager;
    }

    public SeriesOfMeasurements() {
    }

    public void begin(Chart chart) throws PicaxeConnectionErrorException, SpectrometerNotConnected {
        prepareAndStartSeries(chart);
    }

    private void prepareAndStartSeries(Chart chart) {
        double angle = Settings.getInstance().getMeasurementMinAngle();
        Timeline moving;
        if (stepperMotor.currentAngle < angle) {
            moving = new Timeline(new KeyFrame(Duration.millis(stepperMotor.getImpulseTime()), e -> {
                stepperMotor.moveOnePulseForward();
            }));
        } else {
            moving = new Timeline(new KeyFrame(Duration.millis(stepperMotor.getImpulseTime()), e -> {
                stepperMotor.moveOnePulseBackwards();
            }));
        }
        moving.setCycleCount(stepperMotor.pulsesNeededToMove(angle));
        moving.setOnFinished(e -> {
            Timeline waitForLastPulse = new Timeline(new KeyFrame(Duration.millis(stepperMotor.getImpulseTime()+20), e2 -> {
                stepperMotor.stopMotor();
                setupWrapper();
                double[] wavelengths = wrapper.getWavelengths(0);
                chart.setxValues(wavelengths);
                measureAndVisualize(chart, wavelengths); //first measurement
                waitForMeasuring = new Timeline(new KeyFrame(Duration.millis(measurementTime(chart.getDrawingTime())), e1 -> {
                    startSeries(chart, wavelengths);
                }));
                waitForMeasuring.play();
            }));
            waitForLastPulse.play();
        });
        moving.play();
    }

    private void startSeries(Chart chart, double[] wavelengths) {
        Double seriesInterval = seriesInterval(chart.getDrawingTime());

        Double startAngle = Settings.getInstance().getMeasurementMinAngle();
        Double endAngle = Settings.getInstance().getMeasurementMaxAngle();
        Integer stepsToDo = stepperMotor.stepsNeededToMove(endAngle);
        remainingSteps = stepsToDo;
        notifyObservers();

        seriesOfMTimeline = new Timeline(new KeyFrame(Duration.millis(seriesInterval), e -> {
            move(startAngle, endAngle);
            waitForStep = new Timeline(new KeyFrame(Duration.millis(stepperMotor.getStepTime()), e1 -> {
                measureAndVisualize(chart, wavelengths);
            }));
            waitForStep.play();
        }));

        seriesOfMTimeline.setCycleCount(stepsToDo);
        seriesOfMTimeline.setOnFinished(finish -> {
            waitForLastIteration = new Timeline(new KeyFrame(Duration.millis(seriesInterval), e1 -> {
                findAndVisualizeMinValues();
                try {
                    save();
                } catch (ParameterIsNullException parameterIsNullException) {
                    parameterIsNullException.printStackTrace();
                }
                measurementManager.startLiveMode(Settings.getInstance().getIntegrationTime(), chart);
            }));
            waitForLastIteration.play();
        });
        seriesOfMTimeline.play();
    }

    private void move(double startAngle, double endAngle){
        if (startAngle < endAngle) {
            stepperMotor.stepForward();
        } else {
            stepperMotor.stepBackwards();
        }
        remainingSteps--;
        notifyObservers();
    }

    private void setupWrapper(){
        wrapper.setIntegrationTime(0, Settings.getInstance().getIntegrationTime());
        wrapper.setScansToAverage(0, Settings.getInstance().getNumberOfScansToAverage());
    }

    private double seriesInterval(double reserveTime){
        return measurementTime(reserveTime) + stepperMotor.getStepTime();
    }

    private double measurementTime(double reserveTime){
        return (Settings.getInstance().getIntegrationTime() / 1000) * Settings.getInstance().getNumberOfScansToAverage()
                + reserveTime;
    }

    private void findAndVisualizeMinValues(){
        fittedMinimum = new FittedMinimum(this);
        fittedMinimum.visualizeMinValues();
    }

    private void measureAndVisualize(Chart chart, double[] wavelengths) {
        double[] spectralData = wrapper.getSpectrum(0);
        substractBackgroundIfNeeded(spectralData);
        chart.replaceMainData(spectralData, "last measured data");
        try {
            addMeasurement(new Measurement(spectralData, wavelengths, stepperMotor.currentAngle));
        } catch (ParameterIsNullException parameterIsNullException) {
            parameterIsNullException.printStackTrace();
        }
    }

    private void substractBackgroundIfNeeded(double[] values) {
        double[] background = Settings.getInstance().getBackground();
        if (Settings.getInstance().getSubtractBackground() == false || background == null) {
            return;
        }
        for (int i = 0; i < Math.min(values.length, background.length); i++) {
            values[i] = values[i] - background[i];
        }
    }

    public void stop() {
        seriesOfMTimeline.stop();
    }

    public void save() throws ParameterIsNullException {
        if (measurements.isEmpty()) throw new ParameterIsNullException("there are no measurements to save");

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
        for (Measurement m : measurements) {
            try {
                m.saveToFile(seriesDirPath);
            } catch (MissingFolderException | FileDoesNotExistException | FileAlreadyExistsException e) {
                e.printStackTrace();
            }
        }

        //save minimal values
        try {
            fittedMinimum.saveToFile(seriesDirPath);
        } catch (MissingFolderException | FileDoesNotExistException | FileAlreadyExistsException e) {
            e.printStackTrace();
        }
    }

    public void addMeasurement(Measurement m) throws ParameterIsNullException {
        if (m == null) throw new ParameterIsNullException("measurement cannot be null");
        measurements.add(m);
    }


    public void setMainDirPath(String mainDirPath) {
        this.mainDirPath = mainDirPath;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    @Override
    public void attach(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }


}
