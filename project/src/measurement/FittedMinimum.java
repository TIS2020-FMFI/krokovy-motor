package measurement;

import Exceptions.FilesAndFoldersExcetpions.FileAlreadyExistsException;
import Exceptions.FilesAndFoldersExcetpions.FileDoesNotExistException;
import Exceptions.FilesAndFoldersExcetpions.MissingFolderException;
import Exceptions.FilesAndFoldersExcetpions.ParameterIsNullException;
import Jama.Matrix;
import gui.chart.Chart;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import settings.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class FittedMinimum {

    double[][] matrix;

    List<Measurement> measurements;
    int numberOfAngles;
    double[] angles;    //same for every wavelength
    double[] wavelengths;
    double[] minValues;  //minimum for each wavelength

    public FittedMinimum(SeriesOfMeasurements seriesOfMeasurements) {
        measurements = seriesOfMeasurements.getMeasurements();
        numberOfAngles = measurements.size();
        angles = new double[numberOfAngles];
        fillAngles();

        wavelengths = measurements.get(0).getWavelengths();

        matrix = new double[numberOfAngles][wavelengths.length];
        fillMatrix();

        findMinValues();
    }


    private void fillMatrix() {
        for (int i = 0; i < measurements.size(); i++) {
            Measurement m = measurements.get(i);
            for (int j = 0; j < m.getWavelengths().length; j++) {
                matrix[i][j] = m.getSpectrumValues()[j];
            }
        }
    }

    private void fillAngles() {
        int angleIndex = 0;
        for (Measurement measurement : measurements) {
            angles[angleIndex] = measurement.getAngle();
            angleIndex++;
        }
    }

    private void findMinValues() {
        int numOfWavelengths = wavelengths.length;
        minValues = new double[numOfWavelengths];
        for (int i = 0; i < numOfWavelengths; i++) {
            minValues[i] = getMinimum(i);
        }
    }

    private double getMinimum(int wavelengthIndex) {
        return fittedMinimum(angles, collectWavelengthIntensities(wavelengthIndex));
    }

    private double[] collectWavelengthIntensities(int wavelengthIndex) {
        double[] wavelengthIntensities = new double[numberOfAngles];  //intensity on each angle
        int angleIndex = 0;
        for (Measurement measurement : measurements) {
            double[] angleIntensities = measurement.getSpectrumValues();
            wavelengthIntensities[angleIndex] = angleIntensities[wavelengthIndex];
            angleIndex++;
        }
        return wavelengthIntensities;
    }

    private double fittedMinimum(double[] angles, double[] intensities) {
        PolynomialRegression regression = new PolynomialRegression(angles, intensities, 4);
        return functionMinimum2(regression.beta(4), regression.beta(3), regression.beta(2),
                regression.beta(1), regression.beta(0));
    }

    private double functionMinimum(double a, double b, double c, double d, double e) {
        double minValue = Double.POSITIVE_INFINITY;
        double angle = 0;
        for (int i = 0; i < angles.length; i++) {
            double functionValue = getFunctionValue(a, b, c, d, e, angles[i]);
            if (functionValue < minValue) {
                minValue = functionValue;
                angle = angles[i];
            }
        }
        return angle;
    }

    private double functionMinimum2(double a, double b, double c, double d, double e) {
        double minValue = Double.POSITIVE_INFINITY;
        double minAngle = 0;
        Settings settings = Settings.getInstance();
        for (double angle1 = settings.getMeasurementMinAngle(); angle1 < settings.getMeasurementMaxAngle(); angle1 += 0.1) {
            double functionValue = getFunctionValue(a, b, c, d, e, angle1);
            if (functionValue < minValue) {
                minValue = functionValue;
                minAngle = angle1;
            }
        }
        return minAngle;
    }

    private double getFunctionValue(double a, double b, double c, double d, double e, double angle) {
        return a * (Math.pow(angle, 4)) + b * (Math.pow(angle, 3)) + c * (Math.pow(angle, 2)) + d * angle + e;
    }

    public void visualizeMinValues() {

        double[] wlInInterval = getWlInInterval();
        double[] minValsInInterval = getMinValsInInterval();

        Chart chart = new Chart(wlInInterval, "wavelengths", "angles", "Minimal values");
        Stage secondStage = new Stage();
        LineChart chartComponent = chart.getComponent();
        chartComponent.setPrefSize(900, 600);
        Scene scene = new Scene(new HBox(3, chartComponent));
        scene.getStylesheets().add("gui/chart/style.css");
        secondStage.setScene(scene);
        chart.replaceMainData(minValsInInterval, "minimal values - angles");
        secondStage.show();
    }

    private double[] getWlInInterval() {
        Settings settings = Settings.getInstance();
        double[] wlInInterval = new double[intervalSize()];
        int newIndex = 0;
        for (int i = settings.getMinWaveLengthToSave() - 250; i <= settings.getMaxWaveLengthToSave() - 250; i++) {
            wlInInterval[newIndex] = wavelengths[i];
            newIndex++;
        }
        return wlInInterval;
    }

    private double[] getMinValsInInterval() {
        Settings settings = Settings.getInstance();
        double[] minValsInInterval = new double[intervalSize()];
        int newIndex = 0;
        for (int i = settings.getMinWaveLengthToSave() - 250; i <= settings.getMaxWaveLengthToSave() - 250; i++) {
            minValsInInterval[newIndex] = minValues[i];
            newIndex++;
        }
        return minValsInInterval;
    }

    private int intervalSize() {
        Settings settings = Settings.getInstance();
        return Math.abs(settings.getMaxWaveLengthToSave() - settings.getMinWaveLengthToSave() + 1);
    }

    public void saveToFile(String pathToFolder) throws MissingFolderException, FileAlreadyExistsException, FileDoesNotExistException {

        saveMatrix(pathToFolder); //ulozenie matice pre test

        File directory = new File(pathToFolder);
        if (directory.isDirectory() == false) {
            throw new MissingFolderException("Folder for saving measurements does not exist");
        }

        File file = new File(pathToFolder + File.separator + "minimalValues" + ".txt");

        if (file.exists() == true) {
            throw new FileAlreadyExistsException("File for minimal values already exists");
        }
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            throw new FileDoesNotExistException("File for minimal values does not exist"); //ak by zlyhalo vytvorenie
        }

        Settings settings = Settings.getInstance();
        for (int i = 0; i < minValues.length; i++) {
            double wl = wavelengths[i];
            if (wl >= settings.getMinWaveLengthToSave() && wl <= settings.getMaxWaveLengthToSave()) {
                writer.print(wavelengths[i]);
                writer.print("   ");
                writer.print(minValues[i]);
                writer.print(System.lineSeparator());
            }
        }
        writer.flush();
        writer.close();
    }

    public double[] getWavelengths() {
        return wavelengths;
    }

    public double[] getMinValues() {
        return minValues;
    }

    public void saveMatrix(String pathToFolder) throws MissingFolderException, FileAlreadyExistsException, FileDoesNotExistException {
        File directory = new File(pathToFolder);
        if (directory.isDirectory() == false) {
            throw new MissingFolderException("Folder for saving measurements does not exist");
        }

        File file = new File(pathToFolder + File.separator + "matica.txt");

        if (file.exists() == true) {
            throw new FileAlreadyExistsException("File for matrix already exists");
        }
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            throw new FileDoesNotExistException("File for matrix does not exist"); //ak by zlyhalo vytvorenie
        }

        writer.print("GA ");
        for (double w : wavelengths) {
            if (isInInterval(w)) {
                writer.print(w + " ");
            }
        }
        writer.print(System.lineSeparator());
        for (int i = 0; i < matrix.length; i++) {
            double angle = round(angles[i], 4);
            writer.print(angle + " ");
            for (int j = 0; j < matrix[i].length; j++) {
                if (isInInterval(wavelengths[j])) {
                    writer.print(matrix[i][j] + " ");
                }
            }
            writer.print(System.lineSeparator());
        }
        writer.flush();
        writer.close();

    }

    boolean isInInterval(double wavelength) {
        Settings settings = Settings.getInstance();
        return wavelength >= settings.getMinWaveLengthToSave() && wavelength <= settings.getMaxWaveLengthToSave();
    }


    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}