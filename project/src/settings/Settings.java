package settings;

import Exceptions.FilesAndFoldersExcetpions.*;
import com.oceanoptics.omnidriver.api.wrapper.Wrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Settings {
    static Wrapper wrapper;
    private static final int[] allowedIntegrationTimes = new int[]{3, 5 ,10 , 20 , 50 , 100 , 200 , 500 , 1000 , 2000 , 5000 , 10000 , 20000 , 30000 , 50000};

    static Double calibrationMinAngle;
    static Double calibrationMaxAngle;
    static int shiftsSinceCallibrationStart = 0; //posuny ramena od zaciatku kalibracie

    //tieto sa ulozia do suboru config:
    static Boolean isAvereageMode = false;
    static Integer numberOfScansToAverage = 1;
    static String angleUnits = "degrees";
    static Double measurementMinAngle = 0.0;
    static Double measurementMaxAngle = 0.0;
    static String lampParameters = "";
    static Boolean subtractBackground = false;
    static Integer integrationTime = 100;
    static Integer minWaveLengthToSave = 200;
    static Integer maxWaveLengthToSave = 850;
    static Double stepToAngleRatio;
    static String comment = "";



    public Settings() {};


        //TODO spravit kontrolu, ze v ktorych jednotkach sa meria


    public static void saveToFile(String pathToFolder) throws FileAlreadyExistsException, MissingFolderException, FileDoesNotExistException, ParameterIsNullException {
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

        writer.print("minimum angle: " + measurementMinAngle);
        writer.print(System.lineSeparator());

        writer.print("maximum angle: " + measurementMaxAngle);
        writer.print(System.lineSeparator());

        writer.print("lampParameters: " + lampParameters);
        writer.print(System.lineSeparator());

        writer.print("subtractBackground: " + subtractBackground);
        writer.print(System.lineSeparator());

        writer.print("integrationTime: " + integrationTime + " ms");
        writer.print(System.lineSeparator());

        writer.print("minWaveLength: " + minWaveLengthToSave + " nm");
        writer.print(System.lineSeparator());

        writer.print("maxWaveLenth: " + maxWaveLengthToSave + " nm");
        writer.print(System.lineSeparator());

        writer.print("angleStepRatio: " + stepToAngleRatio);
        writer.print(System.lineSeparator());

        writer.flush();
        writer.close();
    }

    //pred zaciatkom serie merani sa moze zavolat
    public static void checkCorrectness() throws WrongParameterException {
        if(stepToAngleRatio == null){
            throw new WrongParameterException("callibration has not been done");
        }

        if(minWaveLengthToSave > maxWaveLengthToSave){
            throw new WrongParameterException("Maximal wavelength must be bigger than minimal wavelength");
        }
        if(minWaveLengthToSave < 200){
            throw new WrongParameterException("minWaveLength parameter must be >= 200 nm");
        }

        if(maxWaveLengthToSave < minWaveLengthToSave){
            throw new WrongParameterException("Maximal wavelength must be bigger than minimal wavelength");
        }
        if(maxWaveLengthToSave > 850){
            throw new WrongParameterException("maxWaveLenth parameter must be <= 850 nm");
        }
    }


    //-----------setters------------------------------------------------------------------------
    public static void setCalibrationMinAngle(Double calibrationMinAngle) throws ParameterIsNullException {
        shiftsSinceCallibrationStart = 0;
        Settings.calibrationMinAngle = calibrationMinAngle;
    }

    public static void setCalibrationMaxAngle(Double calibrationMaxAngle) {
        Settings.calibrationMaxAngle = calibrationMaxAngle;
    }

    public static void setAvereageMode(Boolean avereageMode)  {
        isAvereageMode = avereageMode;
    }

    public static void setNumberOfScansToAverage(Integer numberOfScansToAverage) throws WrongParameterException {
        if(numberOfScansToAverage < 0){
            throw new WrongParameterException("numberOfScansToAverage parameter must be > 0");
        }
        if(numberOfScansToAverage > 200){
            throw new WrongParameterException("numberOfScansToAverage parameter must be <= 200");
        }
        Settings.numberOfScansToAverage = numberOfScansToAverage;
    }

    public static void setAngleUnits(String angleUnits) throws WrongParameterException {
        if(angleUnits.equals("degrees") == false && angleUnits.equals("gradians") == false){
            throw new WrongParameterException("angle units must be either degrees or gradians");
        }
        Settings.angleUnits = angleUnits;
    }

    public static void setMeasurementMinAngle(Double measurementMinAngle)  {
        Settings.measurementMinAngle = measurementMinAngle;
    }

    public static void setMeasurementMaxAngle(Double measurementMaxAngle) {
        Settings.measurementMaxAngle = measurementMaxAngle;
    }

    public static void setLampParameters(String lampParameters) {
        Settings.lampParameters = lampParameters;
    }

    public static void setSubtractBackground(Boolean subtractBackground) {
        Settings.subtractBackground = subtractBackground;
    }

    public static void setIntegrationTime(Integer integrationTime) throws WrongParameterException {
        boolean isAllowed = false;
        for(int allowedTime : allowedIntegrationTimes){
            if(integrationTime.equals(allowedTime)){
                isAllowed = true;
                break;
            }
        }
        if(isAllowed == false) throw new WrongParameterException("Wrong integration time.");
        Settings.integrationTime = integrationTime;
    }

    public static void setMinWaveLengthToSave(Integer minWaveLengthToSave) {
        Settings.minWaveLengthToSave = minWaveLengthToSave;
    }

    public static  void setMaxWaveLengthToSave(Integer maxWaveLengthToSave) {
        Settings.maxWaveLengthToSave = maxWaveLengthToSave;
    }

    public static void setStepToAngleRatio(Double angleStepRatio) {
        Settings.stepToAngleRatio = angleStepRatio;
    }

    public static void setComment(String comment) {
        Settings.comment = comment;
    }

    public static void setIsAvereageMode(Boolean isAvereageMode) {
        Settings.isAvereageMode = isAvereageMode;
    }



    //-----------getters------------------------------------------------------------------------
    public static Double getCalibrationMinAngle() {
        return calibrationMinAngle;
    }

    public static Double getCalibrationMaxAngle() {
        return calibrationMaxAngle;
    }

    public static Boolean getIsAvereageMode() {
        return isAvereageMode;
    }

    public static Integer getNumberOfScansToAverage() {
        return numberOfScansToAverage;
    }

    public static String getAngleUnits() {
        return angleUnits;
    }

    public static Double getMeasurementMinAngle() {
        return measurementMinAngle;
    }

    public static Double getMeasurementMaxAngle() {
        return measurementMaxAngle;
    }

    public static String getLampParameters() {
        return lampParameters;
    }

    public static Boolean getSubtractBackground() {
        return subtractBackground;
    }

    public static Integer getIntegrationTime() {
        return integrationTime;
    }

    public static Integer getMinWaveLengthToSave() {
        return minWaveLengthToSave;
    }

    public static Integer getMaxWaveLengthToSave() {
        return maxWaveLengthToSave;
    }

    public static Double getStepToAngleRatio() {
        return stepToAngleRatio;
    }

    public static String getComment() {
        return comment;
    }

    public int[] getAllowedIntegrationTimes() {
        return allowedIntegrationTimes;
    }
    public static int getShiftsSinceCallibrationStart() {
        return shiftsSinceCallibrationStart;
    }

    public static Wrapper getWrapper() {
        return wrapper;
    }

    public static void main(String[] args) throws ParameterIsNullException, WrongParameterException {
        try {
            Settings.setStepToAngleRatio(1.0);
            Settings.saveToFile("results\\meranie1");
        } catch (FileAlreadyExistsException e) {
            e.printStackTrace();
        } catch (MissingFolderException e) {
            e.printStackTrace();
        } catch (FileDoesNotExistException e) {
            e.printStackTrace();
        }
    }
}
