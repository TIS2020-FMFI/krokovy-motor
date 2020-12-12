package measurement;

import Exceptions.FilesAndFoldersExcetpions.FileAlreadyExistsException;
import Exceptions.FilesAndFoldersExcetpions.FileDoesNotExistException;
import Exceptions.FilesAndFoldersExcetpions.MissingFolderException;
import Exceptions.FilesAndFoldersExcetpions.ParameterIsNullException;
import Jama.Matrix;
import gui.chart.Chart;
import javafx.scene.Scene;
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

    public FittedMinimum(SeriesOfMeasurements seriesOfMeasurements){
        measurements = seriesOfMeasurements.getMeasurements();
        numberOfAngles = measurements.size();
        angles = new double[numberOfAngles];
        fillAngles();

        wavelengths = measurements.get(0).getWavelengths();

        matrix = new double[numberOfAngles][wavelengths.length];
        fillMatrix();

        findMinValues();
    }


    private void fillMatrix(){
        for (int i = 0; i < measurements.size(); i++) {
            Measurement m = measurements.get(i);
            for (int j = 0; j < m.getWavelengths().length; j++) {
                matrix[i][j] = m.getSpectrumValues()[j];
            }
        }
    }

    private void fillAngles(){
        int angleIndex = 0;
        for(Measurement measurement : measurements){
            angles[angleIndex] = measurement.getAngle();
            angleIndex++;
        }
    }

    private void findMinValues(){
        int numOfWavelengths = wavelengths.length;
        minValues = new double[numOfWavelengths];
        for (int i = 0; i < numOfWavelengths; i++) {
            minValues[i] = getMinimum(i);
        }
    }

    private double getMinimum(int wavelengthIndex){
        return fittedMinimum(angles, collectWavelengthIntensities(wavelengthIndex));
    }

    private double[] collectWavelengthIntensities(int wavelengthIndex){
        double[] wavelengthIntensities = new double[numberOfAngles];  //intensity on each angle
        int angleIndex = 0;
        for(Measurement measurement : measurements){
            double[] angleIntensities = measurement.getSpectrumValues();
            wavelengthIntensities[angleIndex] = angleIntensities[wavelengthIndex];
            angleIndex++;
        }
        return wavelengthIntensities;
    }

    private double fittedMinimum(double[] angles, double[] intensities){
        PolynomialRegression regression = new PolynomialRegression(angles, intensities, 2);
        return functionMinimum(regression.beta(2), regression.beta(1), regression.beta(0));
    }

    private double functionMinimum(double a, double b, double c) {
        double minValue = Double.POSITIVE_INFINITY;
        for (int i = 0; i < angles.length; i++) {
            double functionValue = getFunctionValue(a,b,c,angles[i]);
            if(functionValue < minValue){
                minValue = functionValue;
            }
        }
        return minValue;
    }

    private double getFunctionValue(double a, double b, double c, double angle){
        return a*(Math.pow(angle,2)) + b*angle + c;
    }

    public void visualizeMinValues(){
        Chart chart = new Chart(wavelengths, "wavelengths", "minimum", "Minimal values");
        Stage secondStage = new Stage();
        Scene scene = new Scene(new HBox(3, chart.getComponent()), 800,600);
        secondStage.setScene(scene);
        chart.replaceMainData(minValues, "minimal values");
        secondStage.show();
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
            if(wl >= settings.getMinWaveLengthToSave() && wl <= settings.getMaxWaveLengthToSave()) {
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

        int alignValue = 10;


        writer.print("          ");
        for(double w : wavelengths){
            writer.print(align(w, alignValue));
        }
        writer.print(System.lineSeparator());
        for (int i = 0; i < matrix.length; i++) {
            double angle = round(angles[i], 4);
            writer.print(align(angle, alignValue));
            for (int j = 0; j < matrix[i].length; j++) {
                writer.print(align(matrix[i][j], alignValue));
            }
            writer.print(System.lineSeparator());
        }
        writer.flush();
        writer.close();

    }

    private String align(double value, int number){
        String valueString = String.valueOf(value);
        int diff = number - valueString.length();
        for (int i = 0; i < diff; i++) {
            valueString += " ";
        }
        return valueString;
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /*public static void main(String[] args) throws ParameterIsNullException, FileAlreadyExistsException, MissingFolderException, FileDoesNotExistException {
        SeriesOfMeasurements sofm = new SeriesOfMeasurements();
        double[] wavelengts = new double[]{1.0,2.0,3.0};
//        double[] intensities1 = new double[]{3.0,1.0,3.0};
//        double[] intensities2 = new double[]{3.0,1.0,3.0};
//        double[] intensities3 = new double[]{3.0,1.0,3.0};
        double[] intensities1 = new double[]{4,4,4};
        double[] intensities2 = new double[]{1.0,1.0,1.0};
        double[] intensities3 = new double[]{3.0,3.0,3.0};
        sofm.addMeasurement(new Measurement(intensities1, wavelengts, 0));
        sofm.addMeasurement(new Measurement(intensities2, wavelengts, 1));
        sofm.addMeasurement(new Measurement(intensities3, wavelengts, 2));

        FittedMinimum mv = new FittedMinimum(sofm);
        mv.saveMatrix("measuredData");


//        double[] minvals = mv.getMinValues();
//        for (int i = 0; i < minvals.length; i++) {
//            System.out.print(minvals[i] + " ");
//        }
//        System.out.println();
    }*/
}