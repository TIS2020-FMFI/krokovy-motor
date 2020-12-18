package measurement;

import Exceptions.FilesAndFoldersExcetpions.FileAlreadyExistsException;
import Exceptions.FilesAndFoldersExcetpions.FileDoesNotExistException;
import Exceptions.FilesAndFoldersExcetpions.MissingFolderException;
import Exceptions.FilesAndFoldersExcetpions.ParameterIsNullException;
import settings.Settings;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Measurement {

    private final double[] spectrumValues;

    private final double[] wavelengths;

    private final double angle;


    /**
     * @param spectrumValues measured spectrum values
     * @param wavelengths  measured wavelengths
     * @param angle angle on which the data was measured
     * @throws ParameterIsNullException
     */
    public Measurement(double[] spectrumValues, double[] wavelengths, double angle) throws ParameterIsNullException {
        this.spectrumValues = spectrumValues;
        this.wavelengths = wavelengths;
        this.angle = round(angle);

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


    /**
     * saves the measured data into a file
     * @param pathToFolder path to the target folder
     * @throws MissingFolderException
     * @throws FileAlreadyExistsException
     * @throws FileDoesNotExistException
     */
    public void saveToFile(String pathToFolder) throws MissingFolderException, FileAlreadyExistsException, FileDoesNotExistException {

        File directory = new File(pathToFolder);
        if (directory.isDirectory() == false) {
            throw new MissingFolderException("Folder for saving measurements does not exist");
        }

        File file = new File(pathToFolder + File.separator + angle + ".txt");

        if (file.exists() == true) {
            throw new FileAlreadyExistsException("File for this angle already exists"); //ci uz tam nejaky nie je, aby som ho neprepisal
        }
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            throw new FileDoesNotExistException("File for this angle does not exist"); //ak by zlyhalo vytvorenie
        }

        Settings settings = Settings.getInstance();
        for (int i = 0; i < spectrumValues.length; i++) {
            double wl = wavelengths[i];
            if(wl >= settings.getMinWaveLengthToSave() && wl <= settings.getMaxWaveLengthToSave()){
                writer.print(wavelengths[i]);
                writer.print("   ");
                writer.print(spectrumValues[i]);
                writer.print(System.lineSeparator());
            }
        }
        writer.flush();
        writer.close();

    }

    public double[] getSpectrumValues() {
        return spectrumValues;
    }

    public double[] getWavelengths() {
        return wavelengths;
    }

    public double getAngle() {
        return angle;
    }

}
