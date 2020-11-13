package settings;

import Exceptions.FilesAndFoldersExcetpions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Settings {

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
    public static void checkAndSetParameters(Boolean isAvereageMode, Integer numberOfScansToAverage, String angleUnits,
                                              Double measurementMinAngle, Double measurementMaxAngle, String lampParameters,
                                              Boolean subtractBackground, Integer integrationTime, Integer minWaveLengthToSave,
                                              Integer maxWaveLengthToSave, String comment) throws WrongParameterException {
        StringBuilder errorBuilder = new StringBuilder();

        setIsAvereageMode(isAvereageMode);

        try {
            setNumberOfScansToAverage(numberOfScansToAverage);
        } catch (WrongParameterException e) {
            errorBuilder.append(e.getMessage());
        }

        try {
            setAngleUnits(angleUnits);
        } catch (WrongParameterException e) {
            errorBuilder.append(e.getMessage());
        }

        try {
            setMeasurementMinAngle(measurementMinAngle);
        } catch (WrongParameterException e) {
            errorBuilder.append(e.getMessage());
        }

        try {
            setMeasurementMaxAngle(measurementMaxAngle);
        } catch (WrongParameterException e) {
            errorBuilder.append(e.getMessage());
        }

        setLampParameters(lampParameters);

        setSubtractBackground(subtractBackground);

        try {
            setIntegrationTime(integrationTime);
        } catch (WrongParameterException e) {
            errorBuilder.append(e.getMessage());
        }

        try {
            setMinWaveLengthToSave(minWaveLengthToSave);
        } catch (WrongParameterException e) {
            errorBuilder.append(e.getMessage());
        }

        try {
            setMaxWaveLengthToSave(maxWaveLengthToSave);
        } catch (WrongParameterException e) {
            errorBuilder.append(e.getMessage());
        }

        setComment(comment);

        if(errorBuilder.length() == 0){     //ak zatial nie su ziadne chyby
            if(minWaveLengthToSave > maxWaveLengthToSave){
                errorBuilder.append("maximal wavelength must be bigger or the same as minimal wavelength" + "\n");
            }
        }

        if(errorBuilder.length() != 0){
            throw new WrongParameterException(errorBuilder.toString());
        }

    }


    //-----------setters------------------------------------------------------------------------
    public static void setCalibrationMinAngle(Double calibrationMinAngle) throws WrongParameterException {
        if(calibrationMinAngle == null){
            throw new WrongParameterException("calibration starting position cannot be null");
        }
        shiftsSinceCallibrationStart = 0;
        Settings.calibrationMinAngle = calibrationMinAngle;
    }

    public static void setCalibrationMaxAngle(Double calibrationMaxAngle) throws WrongParameterException {
        if(calibrationMaxAngle == null){
            throw new WrongParameterException("calibration ending position cannot be null");
        }
        Settings.calibrationMaxAngle = calibrationMaxAngle;
    }

    private static void setNumberOfScansToAverage(Integer numberOfScansToAverage) throws WrongParameterException {
        if(isAvereageMode && numberOfScansToAverage == null){
            throw new WrongParameterException("number of scans to average are not set" + "\n");
        }
        if(numberOfScansToAverage != null){
            if(numberOfScansToAverage < 0){
                throw new WrongParameterException("numberOfScansToAverage parameter must be > 0" + "\n");
            }
            if(numberOfScansToAverage > 200){
                throw new WrongParameterException("numberOfScansToAverage parameter must be <= 200" + "\n");
            }
        }
        Settings.numberOfScansToAverage = numberOfScansToAverage;
    }

    private static void setAngleUnits(String angleUnits) throws WrongParameterException {
        if(angleUnits == null){
            throw new WrongParameterException("angle units are not set" + "\n");
        }
        if(angleUnits.equals("degrees") == false && angleUnits.equals("gradians") == false){
            throw new WrongParameterException("angle units must be either degrees or gradians" + "\n");
        }
        Settings.angleUnits = angleUnits;
    }

    private static void setMeasurementMinAngle(Double measurementMinAngle) throws WrongParameterException {
        if(measurementMinAngle == null){
            throw new WrongParameterException("the measurement starting position is not set" + "\n");
        }
        if(measurementMinAngle < 0){
            throw new WrongParameterException("the measurement starting position must be > 0" + "\n");
        }
        if(angleUnits.equals("gradians") && measurementMinAngle > 180){
            throw new WrongParameterException("the measurement starting position must be <= 180");
        }
        if(angleUnits.equals("degrees") && measurementMinAngle > 162){
            throw new WrongParameterException("the measurement starting position must be <= 162");
        }
        Settings.measurementMinAngle = measurementMinAngle;
    }

    private static void setMeasurementMaxAngle(Double measurementMaxAngle) throws WrongParameterException {
        if(measurementMaxAngle == null){
            throw new WrongParameterException("the measurement ending position is not set" + "\n");
        }
        if(measurementMaxAngle < 0){
            throw new WrongParameterException("the measurement ending position must be > 0" + "\n");
        }
        if(angleUnits.equals("gradians") && measurementMaxAngle > 180){
            throw new WrongParameterException("the measurement ending position must be <= 180");
        }
        if(angleUnits.equals("degrees") && measurementMaxAngle > 162){
            throw new WrongParameterException("the measurement ending position must be <= 162");
        }
        Settings.measurementMaxAngle = measurementMaxAngle;
    }

    private static void setLampParameters(String lampParameters) {
        Settings.lampParameters = lampParameters == null? "" : lampParameters;
    }

    private static void setSubtractBackground(Boolean subtractBackground){
        Settings.subtractBackground = subtractBackground == null? false : subtractBackground;
    }

    private static void setIntegrationTime(Integer integrationTime) throws WrongParameterException {
        if(integrationTime == null){
            throw new WrongParameterException("integration time is not set" + "\n");
        }

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

    private static void setMinWaveLengthToSave(Integer minWaveLengthToSave) throws WrongParameterException {
        if(minWaveLengthToSave == null){
            throw new WrongParameterException("minimal wavelength to save is not set" + "\n");
        }
        if(minWaveLengthToSave < 200){
            throw new WrongParameterException("minimal wavelength to save must be >= 200 nm" + "\n");
        }
        if(minWaveLengthToSave > 850){
            throw new WrongParameterException("minimal wavelength to save must be <= 850 nm" + "\n");
        }
        Settings.minWaveLengthToSave = minWaveLengthToSave;
    }

    private static  void setMaxWaveLengthToSave(Integer maxWaveLengthToSave) throws WrongParameterException {
        if(maxWaveLengthToSave == null){
            throw new WrongParameterException("maximal wavelength to save is not set" + "\n");
        }
        if(maxWaveLengthToSave < 200){
            throw new WrongParameterException("maximal wavelength to save must be >= 200 nm" + "\n");
        }
        if(maxWaveLengthToSave > 850){
            throw new WrongParameterException("maximal wavelength to save must be <= 850 nm" + "\n");
        }
        Settings.maxWaveLengthToSave = maxWaveLengthToSave;
    }

    private static void setStepToAngleRatio(Double angleStepRatio) throws WrongParameterException {
        if(stepToAngleRatio == null){
            throw new WrongParameterException("calibration has to be done before measuring" + "\n");
        }
        Settings.stepToAngleRatio = angleStepRatio;
    }

    private static void setComment(String comment) {
        Settings.comment = comment == null? "" : comment;
    }

    private static void setIsAvereageMode(Boolean isAvereageMode) {
        Settings.isAvereageMode = isAvereageMode == null? false : isAvereageMode;
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


//    public static void main(String[] args) throws ParameterIsNullException, WrongParameterException {
//        try {
//            Settings.setStepToAngleRatio(1.0);
//            Settings.saveToFile("results\\meranie1");
//        } catch (FileAlreadyExistsException e) {
//            e.printStackTrace();
//        } catch (MissingFolderException e) {
//            e.printStackTrace();
//        } catch (FileDoesNotExistException e) {
//            e.printStackTrace();
//        }
//    }
}
