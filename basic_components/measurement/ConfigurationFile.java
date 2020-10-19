package measurement;

import Exceptions.FilesAndFoldersExcetpions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Set;

public class ConfigurationFile {

    Boolean isAvereageMode;
    Integer numberOfScansToAverage;
    String angleUnits;
    Double minAngle;
    Double maxAngle;
    String lampParameters;
    Boolean subtractBackground;
    Integer integrationTime;
    Integer minWaveLength;
    Integer maxWaveLenth;
    Double angleStepRatio;

    public ConfigurationFile(Boolean isAvereageMode, Integer numberOfScansToAverage, String angleUnits, Double minAngle,
                             Double maxAngle, String lampParameters, Boolean subtractBackground, Integer integrationTime,
                             Integer minWaveLength, Integer maxWaveLenth, Double angleStepRatio)
            throws ParameterIsNullException, WrongParameterException {

        if (isAvereageMode == null) {
            throw new ParameterIsNullException("isAvereageMode parameter cannot be null");
        }
        if (numberOfScansToAverage == null) {
            throw new ParameterIsNullException("numberOfScansToAverage parameter cannot be null");
        }
        if (angleUnits == null) {
            throw new ParameterIsNullException("angleUnits parameter cannot be null");
        }
        if (minAngle == null) {
            throw new ParameterIsNullException("minAngle parameter cannot be null");
        }
        if (maxAngle == null) {
            throw new ParameterIsNullException("maxAngle parameter cannot be null");
        }
        if (lampParameters == null) {
            throw new ParameterIsNullException("lampParameters parameter cannot be null");
        }
        if (subtractBackground == null) {
            throw new ParameterIsNullException("subtractBackground parameter cannot be null");
        }
        if (integrationTime == null) {
            throw new ParameterIsNullException("integrationTime parameter cannot be null");
        }
        if (minWaveLength == null) {
            throw new ParameterIsNullException("minWaveLength parameter cannot be null");
        }
        if (maxWaveLenth == null) {
            throw new ParameterIsNullException("maxWaveLenth parameter cannot be null");
        }
        if (angleStepRatio == null) {
            throw new ParameterIsNullException("angleStepRatio parameter cannot be null");
        }


        if(numberOfScansToAverage < 0){
            throw new WrongParameterException("numberOfScansToAverage parameter must be > 0");
        }

        if(numberOfScansToAverage > 200){
            throw new WrongParameterException("numberOfScansToAverage parameter must be <= 200");
        }

        if(Set.of(3, 5 ,10 , 20 , 50 , 100 , 200 , 500 , 1000 , 2000 , 5000 , 10000 , 20000 , 30000 , 50000).contains(integrationTime) == false){
            throw new WrongParameterException("Wrong integration time.");
        }

        if(minWaveLength > maxWaveLenth){
            throw new WrongParameterException("Maximal wavelength must be bigger than minimal wavelength");
        }
        if(minWaveLength < 200){
            throw new WrongParameterException("minWaveLength parameter must be >= 200 nm");
        }

        if(maxWaveLenth > 850){
            throw new WrongParameterException("maxWaveLenth parameter must be <= 850 nm");
        }


        this.isAvereageMode = isAvereageMode;
        this.numberOfScansToAverage = numberOfScansToAverage;
        this.angleUnits = angleUnits;
        this.minAngle = minAngle;
        this.maxAngle = maxAngle;
        this.lampParameters = lampParameters;
        this.subtractBackground = subtractBackground;
        this.integrationTime = integrationTime;
        this.minWaveLength = minWaveLength;
        this.maxWaveLenth = maxWaveLenth;
        this.angleStepRatio = angleStepRatio;
    }

    public void saveToFile(String pathToFolder) throws FileAlreadyExistsException, MissingFolderException, FileDoesNotExistException, ParameterIsNullException {
        if (pathToFolder == null) {
            throw new ParameterIsNullException("pathToFolder parameter cannot be null");
        }

        File directory = new File(pathToFolder);
        if (directory.isDirectory() == false) {
            throw new MissingFolderException("Folder for saving measurements does not exist");
        }

        File file = new File(pathToFolder + File.separator + "configuration.txt");

        if (file.exists() == true) {
            throw new FileAlreadyExistsException("File for configuration file already exists"); //ci uz tam nejaky nie je, aby som ho neprepisal
        }
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            throw new FileDoesNotExistException("File for configuration file does not exist"); //ak by zlyhalo vytvorenie
        }

        if (isAvereageMode == true) {
            writer.print("modeOfMeasurement: average spectrum mode");
            writer.print(System.lineSeparator());
            writer.print("numberOfScansToAverage: " + numberOfScansToAverage);
            writer.print(System.lineSeparator());
        } else {
            writer.print("modeOfMeasurement: current spectrum mode");
            writer.print(System.lineSeparator());
        }

        writer.print("angleUnits: " + angleUnits);
        writer.print(System.lineSeparator());

        writer.print("minimum angle: " + minAngle);
        writer.print(System.lineSeparator());

        writer.print("maximum angle: " + maxAngle);
        writer.print(System.lineSeparator());

        writer.print("lampParameters: " + lampParameters);
        writer.print(System.lineSeparator());

        writer.print("subtractBackground: " + subtractBackground);
        writer.print(System.lineSeparator());

        writer.print("integrationTime: " + integrationTime + " ms");
        writer.print(System.lineSeparator());

        writer.print("minWaveLength: " + minWaveLength + " nm");
        writer.print(System.lineSeparator());

        writer.print("maxWaveLenth: " + maxWaveLenth + " nm");
        writer.print(System.lineSeparator());

        writer.print("angleStepRatio: " + angleStepRatio);
        writer.print(System.lineSeparator());

        writer.flush();
        writer.close();

    }

    public static void main(String[] args) throws ParameterIsNullException, WrongParameterException {
        ConfigurationFile c = new ConfigurationFile(true, 10, "gradians", 0d,
                120d, "wolframova halogenova lampa, 10 voltov, 10 amperov, velmi dobra lampa", false,
                50, 200, 400, 0.5);

        try {
            c.saveToFile("results\\meranie1");
        } catch (FileAlreadyExistsException e) {
            e.printStackTrace();
        } catch (MissingFolderException e) {
            e.printStackTrace();
        } catch (FileDoesNotExistException e) {
            e.printStackTrace();
        }
    }
}
