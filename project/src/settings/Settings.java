package settings;

import Exceptions.FilesAndFoldersExcetpions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Settings {

    private static final int[] allowedIntegrationTimes = new int[]{3, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 30000, 50000};

    public static int stepSize;

    static Double calibrationMinAngle;
    static Double calibrationMaxAngle;
    public static int stepsSinceCalibrationStart = 0; //kolko krokov sa spravilo od zaciatku kalibracie

    //tieto sa ulozia do suboru config:
    static Boolean isAvereageMode = false;
    static Integer numberOfScansToAverage = 1;
    static String angleUnits = "degrees";
    static Double measurementMinAngle;
    static Double measurementMaxAngle;
    static String lampParameters = "";
    static Boolean subtractBackground = false;
    static Integer integrationTime = 100;
    static Integer minWaveLengthToSave = 200;
    static Integer maxWaveLengthToSave = 850;
    static Double pulseToAngleRatio; //1 pulse == pulseToAngleRatio degrees/gradians
    static String comment = "";


    public Settings() {
    }

    ;


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

        writer.print("angleStepRatio: " + pulseToAngleRatio);
        writer.print(System.lineSeparator());

        writer.flush();
        writer.close();
    }

    //pred zaciatkom serie merani sa musi zavolat
    public static void checkAndSetParameters(Boolean isAvereageMode, Integer numberOfScansToAverage, String angleUnits, //min,max obe dvojice a stepsize asi string
                                             Double measurementMinAngle, Double measurementMaxAngle, String lampParameters,
                                             Boolean subtractBackground, Integer integrationTime, Integer minWaveLengthToSave,
                                             Integer maxWaveLengthToSave, String comment, Integer stepSize) throws WrongParameterException {
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

        try {
            setStepSize(stepSize);
        } catch (WrongParameterException e) {
            errorBuilder.append(e.getMessage());
        }

        if (minWaveLengthToSave != null && maxWaveLengthToSave != null) {
            if (minWaveLengthToSave > maxWaveLengthToSave) {
                errorBuilder.append("maximal wavelength must be bigger or the same as minimal wavelength" + "\n");
            }
        }

        if (pulseToAngleRatio == null) {
            errorBuilder.append("calibration has to be done before measuring" + "\n");
        }

        if (errorBuilder.length() != 0) {
            throw new WrongParameterException(errorBuilder.toString());
        }

    }


    //-----------setters------------------------------------------------------------------------
    public static void setCalibrationMinAngle(String calibrationMinAngle) throws WrongParameterException {

        Double minAngle;

        try {
            minAngle = Double.parseDouble(calibrationMinAngle);
        } catch (NumberFormatException e) {
            throw new WrongParameterException("you must enter correct double value");
        }
        if (minAngle < 0) {
            throw new WrongParameterException("calibration starting position must be >= 0");
        }
        if (minAngle > 162) {
            throw new WrongParameterException("calibration starting position must be <= 162");
        }
        stepsSinceCalibrationStart = 0;
        Settings.calibrationMinAngle = minAngle;
    }

    public static void setCalibrationMaxAngle(String calibrationMaxAngle) throws WrongParameterException {

        Double maxAngle;

        try {
            maxAngle = Double.parseDouble(calibrationMaxAngle);
        } catch (NumberFormatException e) {
            throw new WrongParameterException("you must enter correct double value");
        }
        if (maxAngle < 0) {
            throw new WrongParameterException("calibration ending position must be >= 0");
        }
        if (maxAngle > 162) {
            throw new WrongParameterException("calibration ending position must be <= 162");
        }

        if(calibrationMinAngle == null){
            throw new WrongParameterException("you must enter start position for calibration first");
        }

        pulseToAngleRatio = Math.abs(maxAngle - calibrationMinAngle) / (stepsSinceCalibrationStart * stepSize);
        Settings.calibrationMaxAngle = maxAngle;
    }

    private static void setNumberOfScansToAverage(Integer numberOfScansToAverage) throws WrongParameterException {
        if (isAvereageMode && numberOfScansToAverage == null) {
            throw new WrongParameterException("number of scans to average are not set" + "\n");
        }
        if (numberOfScansToAverage != null) {
            if (numberOfScansToAverage < 0) {
                throw new WrongParameterException("numberOfScansToAverage parameter must be > 0" + "\n");
            }
            if (numberOfScansToAverage > 200) {
                throw new WrongParameterException("numberOfScansToAverage parameter must be <= 200" + "\n");
            }
        }
        Settings.numberOfScansToAverage = numberOfScansToAverage;
    }

    private static void setAngleUnits(String angleUnits) throws WrongParameterException {
        if (angleUnits == null) {
            throw new WrongParameterException("angle units are not set" + "\n");
        }
        if (angleUnits.equals("degrees") == false && angleUnits.equals("gradians") == false) {
            throw new WrongParameterException("angle units must be either degrees or gradians" + "\n");
        }
        Settings.angleUnits = angleUnits;
    }

    private static void setMeasurementMinAngle(Double measurementMinAngle) throws WrongParameterException {
        if (measurementMinAngle == null) {
            throw new WrongParameterException("the measurement starting position is not set" + "\n");
        }
        if (measurementMinAngle < 0) {
            throw new WrongParameterException("the measurement starting position must be >= 0" + "\n");
        }
        if (angleUnits.equals("gradians") && measurementMinAngle > 180) {
            throw new WrongParameterException("the measurement starting position must be <= 180");
        }
        if (angleUnits.equals("degrees") && measurementMinAngle > 162) {
            throw new WrongParameterException("the measurement starting position must be <= 162");
        }
        Settings.measurementMinAngle = measurementMinAngle;
    }

    private static void setMeasurementMaxAngle(Double measurementMaxAngle) throws WrongParameterException {
        if (measurementMaxAngle == null) {
            throw new WrongParameterException("the measurement ending position is not set" + "\n");
        }
        if (measurementMaxAngle < 0) {
            throw new WrongParameterException("the measurement ending position must be >= 0" + "\n");
        }
        if (angleUnits.equals("gradians") && measurementMaxAngle > 180) {
            throw new WrongParameterException("the measurement ending position must be <= 180");
        }
        if (angleUnits.equals("degrees") && measurementMaxAngle > 162) {
            throw new WrongParameterException("the measurement ending position must be <= 162");
        }
        Settings.measurementMaxAngle = measurementMaxAngle;
    }

    private static void setLampParameters(String lampParameters) {
        Settings.lampParameters = lampParameters == null ? "" : lampParameters;
    }

    private static void setSubtractBackground(Boolean subtractBackground) {
        Settings.subtractBackground = subtractBackground == null ? false : subtractBackground;
    }

    private static void setIntegrationTime(Integer integrationTime) throws WrongParameterException {
        if (integrationTime == null) {
            throw new WrongParameterException("integration time is not set" + "\n");
        }

        boolean isAllowed = false;
        for (int allowedTime : allowedIntegrationTimes) {
            if (integrationTime.equals(allowedTime)) {
                isAllowed = true;
                break;
            }
        }
        if (isAllowed == false) throw new WrongParameterException("Wrong integration time.");
        Settings.integrationTime = integrationTime;
    }

    private static void setMinWaveLengthToSave(Integer minWaveLengthToSave) throws WrongParameterException {
        if (minWaveLengthToSave == null) {
            throw new WrongParameterException("minimal wavelength to save is not set" + "\n");
        }
        if (minWaveLengthToSave < 200) {
            throw new WrongParameterException("minimal wavelength to save must be >= 200 nm" + "\n");
        }
        if (minWaveLengthToSave > 850) {
            throw new WrongParameterException("minimal wavelength to save must be <= 850 nm" + "\n");
        }
        Settings.minWaveLengthToSave = minWaveLengthToSave;
    }

    private static void setMaxWaveLengthToSave(Integer maxWaveLengthToSave) throws WrongParameterException {
        if (maxWaveLengthToSave == null) {
            throw new WrongParameterException("maximal wavelength to save is not set" + "\n");
        }
        if (maxWaveLengthToSave < 200) {
            throw new WrongParameterException("maximal wavelength to save must be >= 200 nm" + "\n");
        }
        if (maxWaveLengthToSave > 850) {
            throw new WrongParameterException("maximal wavelength to save must be <= 850 nm" + "\n");
        }
        Settings.maxWaveLengthToSave = maxWaveLengthToSave;
    }

    private static void setPulseToAngleRatio(Double pulseToAngleRatio) throws WrongParameterException {
        if (pulseToAngleRatio == null) {
            throw new WrongParameterException("shift to angle ratio cannot be null");
        }
        Settings.pulseToAngleRatio = pulseToAngleRatio;
    }

    private static void setComment(String comment) {
        Settings.comment = comment == null ? "" : comment;
    }

    private static void setIsAvereageMode(Boolean isAvereageMode) {
        Settings.isAvereageMode = isAvereageMode == null ? false : isAvereageMode;
    }

    public static void setStepSize(Integer stepSize) throws WrongParameterException {
        if (stepSize == null) {
            throw new WrongParameterException("step size is not set" + "\n");
        }
        if (stepSize < 1) {
            throw new WrongParameterException("the number of impulses in one step must be >= 1" + "\n");
        }
        if (stepSize > 400) {
            throw new WrongParameterException("the number of impulses in one step must be <= 400" + "\n");
        }
        Settings.stepSize = stepSize;
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

    public static Double getPulseToAngleRatio() {
        return pulseToAngleRatio;
    }

    public static String getComment() {
        return comment;
    }

    public int[] getAllowedIntegrationTimes() {
        return allowedIntegrationTimes;
    }

    public static int getStepsSinceCalibrationStart() {
        return stepsSinceCalibrationStart;
    }

    public static int getStepSize() {
        return stepSize;
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
