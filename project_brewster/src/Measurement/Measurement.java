package measurement;

import Exceptions.FilesAndFoldersExcetpions.FileAlreadyExistsException;
import Exceptions.FilesAndFoldersExcetpions.FileDoesNotExistException;
import Exceptions.FilesAndFoldersExcetpions.MissingFolderException;
import Exceptions.FilesAndFoldersExcetpions.ParameterIsNullException;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Measurement {

    private final double[] spectrumValues;

    private final int[] wavelengths;

    private final double angle;

    private final String pathToFolder;

    public Measurement(double[] spectrumValues, int[] wavelengths, double angle, String pathToFolder) throws ParameterIsNullException {
        this.spectrumValues = spectrumValues;
        this.wavelengths = wavelengths;
        this.angle = round(angle);
        this.pathToFolder = pathToFolder;

        if (this.spectrumValues == null) {
            throw new ParameterIsNullException("Array of spectrum values cannot be null");
        }

        if (this.wavelengths == null) {
            throw new ParameterIsNullException("Array of wavelengths cannot be null");
        }

        if (this.spectrumValues.length != this.wavelengths.length) {
            throw new RuntimeException("Arrays of spectrum values and wavelengths must have same size");
        }
    }

    // uhol sa ma vzdy zakokruhlit na 4 desatinne miesta
    private double round(double value) {
        long factor = (long) Math.pow(10, 4);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


    public void saveToFile() throws MissingFolderException, FileAlreadyExistsException, FileDoesNotExistException {

        File directory = new File(pathToFolder);
//        if (directory.isDirectory() == false) {
//            throw new MissingFolderException("Folder for saving measurements does not exist");
//        }
//
        File file = new File(pathToFolder + File.separator + angle + ".txt");
//
        if (file.exists() == true) {
            throw new FileAlreadyExistsException("File for this angle already exists"); //ci uz tam nejaky nie je, aby som ho neprepisal
        }
//        PrintWriter writer = null;
//
//        try {
//            writer = new PrintWriter(file);
//        } catch (FileNotFoundException e) {
//            throw new FileDoesNotExistException("File for this angle does not exist"); //ak by zlyhalo vytvorenie
//        }
//
//        for (int i = 0; i < spectrumValues.length; i++) {
//            writer.print(wavelengths[i]);
//            writer.print(" ");
//            writer.print(spectrumValues[i]);
//            writer.print(System.lineSeparator());
//        }
//        writer.flush();
//        writer.close();

    }

    public static void main(String[] args) {
        int[] waveLengths = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        double[] values = {100.78, 150.01, 200.8, 300.0, 50.5, 300, 222.2, 134.12, 123.10, 99.99};
        double angle = 90.123457;
        Measurement m = null;
        try {
            m = new Measurement(values, waveLengths, angle, "results\\meranie1");
        } catch (ParameterIsNullException e) {
            System.out.println(e.getMessage());
        }
        try {
            m.saveToFile();
        } catch (MissingFolderException | FileAlreadyExistsException | FileDoesNotExistException e) {
            System.out.println(e.getMessage());
        }
    }

}
