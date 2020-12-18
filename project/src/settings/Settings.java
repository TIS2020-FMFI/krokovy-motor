package settings;

import Exceptions.FilesAndFoldersExcetpions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Settings {

    private static Settings instance = null;

    public double[] background;
    public int stepSize;

    public final int DEGREES_MAX = 162;
    public final int GRADIANS_MAX = 180;
    private final int NUMBER_OF_SCANS_MIN = 1;
    private final int NUMBER_OF_SCANS_MAX = 200;
    private final int[] allowedIntegrationTimes = {3000, 5000, 10000, 20000, 50000, 100000, 200000, 500000, 1000000, 2000000, 5000000, 10000000, 20000000, 30000000, 50000000};
    private Double calibrationMinAngle;
    private Double calibrationMaxAngle;
    public int pulsesSinceCalibrationStart = 0; // kolko krokov sa spravilo od zaciatku kalibracie

    // tieto sa ulozia do suboru config:
    private Boolean isAvereageMode = false;
    private Integer numberOfScansToAverage = 1;
    private String angleUnits = "degrees";
    private Double measurementMinAngle;
    private Double measurementMaxAngle;
    private String lampParameters = "";
    private Boolean subtractBackground = false;
    private Integer integrationTime = 100;
    private Integer minWaveLengthToSave = 200;
    private Integer maxWaveLengthToSave = 850;
    private Double pulseToAngleRatio; //1 pulse == pulseToAngleRatio degrees/gradians
    private String comment = "";

    private Settings() {
    }

    /**
     * @return class instance
     */
    public static Settings getInstance() {

        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public boolean isCalibrationSet() {

        return pulseToAngleRatio != null;
    }

    /**
     * saves the settings into file
     * @param pathToFolder target folder
     * @throws FileAlreadyExistsException
     * @throws MissingFolderException
     * @throws FileDoesNotExistException
     * @throws ParameterIsNullException
     */
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

        writer.print("minimum angle: " + measurementMinAngle);
        writer.print(System.lineSeparator());

        writer.print("maximum angle: " + measurementMaxAngle);
        writer.print(System.lineSeparator());

        writer.print("lampParameters: " + lampParameters);
        writer.print(System.lineSeparator());

        writer.print("subtractBackground: " + subtractBackground);
        writer.print(System.lineSeparator());

        writer.print("integrationTime: " + integrationTime/1000 + " ms");
        writer.print(System.lineSeparator());

        writer.print("minWaveLength: " + minWaveLengthToSave + " nm");
        writer.print(System.lineSeparator());

        writer.print("maxWaveLenth: " + maxWaveLengthToSave + " nm");
        writer.print(System.lineSeparator());

        writer.print("pulseToAngleRatio: " + pulseToAngleRatio);
        writer.print(System.lineSeparator());

        writer.print("comment: " + comment);
        writer.print(System.lineSeparator());

        writer.flush();
        writer.close();
    }

    /**
     * checks if all given parameters are correct, sets them if they are
     * else throws an exception
     * @param isAvereageMode
     * @param numberOfScansToAverage
     * @param angleUnits
     * @param measurementMinAngle
     * @param measurementMaxAngle
     * @param lampParameters
     * @param subtractBackground
     * @param integrationTime
     * @param minWaveLengthToSave
     * @param maxWaveLengthToSave
     * @param comment
     * @param stepSize
     * @throws WrongParameterException
     */
    public void checkAndSetParameters(Boolean isAvereageMode, String numberOfScansToAverage, String angleUnits, // min,max obe dvojice a stepsize asi string
                                      String measurementMinAngle, String measurementMaxAngle, String lampParameters,
                                      Boolean subtractBackground, Integer integrationTime, String minWaveLengthToSave,
                                      String maxWaveLengthToSave, String comment, String stepSize) throws WrongParameterException {

        StringBuilder errorBuilder = new StringBuilder();

        setIsAvereageMode(isAvereageMode);

        try {
            setNumberOfScansToAverage(numberOfScansToAverage);
        } catch (WrongParameterException e) {
            errorBuilder.append(e.getMessage());
            errorBuilder.append("\n\n");
        }

        try {
            setAngleUnits(angleUnits);
        } catch (WrongParameterException e) {
            errorBuilder.append(e.getMessage());
            errorBuilder.append("\n\n");
        }

        try {
            setMeasurementMinAngle(measurementMinAngle);
        } catch (WrongParameterException e) {
            errorBuilder.append(e.getMessage());
            errorBuilder.append("\n\n");
        }

        try {
            setMeasurementMaxAngle(measurementMaxAngle);
        } catch (WrongParameterException e) {
            errorBuilder.append(e.getMessage());
            errorBuilder.append("\n\n");
        }

        setLampParameters(lampParameters);

        setSubtractBackground(subtractBackground);

        try {
            setIntegrationTime(integrationTime);
        } catch (WrongParameterException e) {
            errorBuilder.append(e.getMessage());
            errorBuilder.append("\n\n");
        }

        try {
            setMinWaveLengthToSave(minWaveLengthToSave);
        } catch (WrongParameterException e) {
            errorBuilder.append(e.getMessage());
            errorBuilder.append("\n\n");
        }

        try {
            setMaxWaveLengthToSave(maxWaveLengthToSave);
        } catch (WrongParameterException e) {
            errorBuilder.append(e.getMessage());
            errorBuilder.append("\n\n");
        }

        setComment(comment);

        try {
            setStepSize(stepSize);
        } catch (WrongParameterException e) {
            errorBuilder.append(e.getMessage());
            errorBuilder.append("\n\n");
        }

        if (minWaveLengthToSave != null && maxWaveLengthToSave != null) {
            if (this.minWaveLengthToSave > this.maxWaveLengthToSave) {
                errorBuilder.append("maximal wavelength must be bigger or the same as minimal wavelength");
                errorBuilder.append("\n\n");
            }
        }

        if (pulseToAngleRatio == null) {
            errorBuilder.append("calibration has to be done before measuring");
            errorBuilder.append("\n\n");
        }

        if (errorBuilder.length() != 0) {
            throw new WrongParameterException(errorBuilder.toString());
        }

    }


    public void incrementStepSize(){
        stepSize ++;
    }
    public void decrementStepSize(){
        stepSize --;
        stepSize = Math.max(1, stepSize);
    }


    //-----------setters------------------------------------------------------------------------
    public void setCalibrationMinAngle(String calibrationMinAngle) throws WrongParameterException {

        Double minAngle;

        try {
            minAngle = Double.parseDouble(calibrationMinAngle);
        } catch (NumberFormatException e) {
            throw new WrongParameterException("calibration starting position is in wrong format");
        }
        if (minAngle < 0) {
            throw new WrongParameterException("calibration starting position must be >= 0");
        }
        if (minAngle > 162) {
            throw new WrongParameterException("calibration starting position must be <= 162");
        }
        pulsesSinceCalibrationStart = 0;
        this.calibrationMinAngle = minAngle;
    }

    public void setCalibrationMaxAngle(String calibrationMaxAngle) throws WrongParameterException {

        Double maxAngle;

        try {
            maxAngle = Double.parseDouble(calibrationMaxAngle);
        } catch (NumberFormatException e) {
            throw new WrongParameterException("calibration ending position is in wrong format");
        }
        if (maxAngle < 0) {
            throw new WrongParameterException("calibration ending position must be >= 0");
        }
        if (maxAngle > 162) {
            throw new WrongParameterException("calibration ending position must be <= 162");
        }

        if (calibrationMinAngle == null) {
            throw new WrongParameterException("you must enter start position for calibration first");
        }

        pulseToAngleRatio = Math.abs(maxAngle - calibrationMinAngle) / (pulsesSinceCalibrationStart);
        this.calibrationMaxAngle = maxAngle;
    }

    public void setNumberOfScansToAverage(String numberOfScansToAverage) throws WrongParameterException {

        Integer value;
        if (numberOfScansToAverage == null || numberOfScansToAverage.equals("")) {
            this.numberOfScansToAverage = 1;
            return;
        }
        try {
            value = Integer.valueOf(numberOfScansToAverage);
        } catch (NumberFormatException e) { //ak pouzivatel nechtiac zada pismeno, nech nevyskakuje alert
            this.numberOfScansToAverage = 1;
            return;
        }
        if (value < NUMBER_OF_SCANS_MIN) {
            throw new WrongParameterException("number of scans to average must be >= " + NUMBER_OF_SCANS_MIN);
        }
        if (value > NUMBER_OF_SCANS_MAX) {
            throw new WrongParameterException("number of scans to average must be <= " + NUMBER_OF_SCANS_MAX);
        }
        this.numberOfScansToAverage = value;
    }

    private void setAngleUnits(String angleUnits) throws WrongParameterException {

        if (angleUnits == null) {
            throw new WrongParameterException("angle units are not set");
        }
        if (angleUnits.equals("degrees") == false && angleUnits.equals("gradians") == false) {
            throw new WrongParameterException("angle units must be either degrees or gradians");
        }
        this.angleUnits = angleUnits;
    }

    private void setMeasurementMinAngle(String measurementMinAngle) throws WrongParameterException {

        Double value;
        if (measurementMinAngle == null) {
            throw new WrongParameterException("the measurement starting position is not set");
        }
        try {
            value = Double.parseDouble(measurementMinAngle);
        } catch (NumberFormatException e) {
            throw new WrongParameterException("the measurement starting position is in wrong format");
        }
        if (value < 0) {
            throw new WrongParameterException("the measurement starting position must be >= 0");
        }
        if (angleUnits.equals("gradians") && value > GRADIANS_MAX) {
            throw new WrongParameterException("the measurement starting position must be <= " + GRADIANS_MAX);
        }
        if (angleUnits.equals("degrees") && value > DEGREES_MAX) {
            throw new WrongParameterException("the measurement starting position must be <= " + DEGREES_MAX);
        }
        this.measurementMinAngle = value;
    }

    private void setMeasurementMaxAngle(String measurementMaxAngle) throws WrongParameterException {

        Double value;
        if (measurementMaxAngle == null) {
            throw new WrongParameterException("the measurement ending position is not set");
        }
        try {
            value = Double.parseDouble(measurementMaxAngle);
        } catch (NumberFormatException e) {
            throw new WrongParameterException("the measurement ending position is in wrong format");
        }
        if (value < 0) {
            throw new WrongParameterException("the measurement ending position must be >= 0");
        }
        if (angleUnits.equals("gradians") && value > GRADIANS_MAX) {
            throw new WrongParameterException("the measurement ending position must be <= " + GRADIANS_MAX);
        }
        if (angleUnits.equals("degrees") && value > DEGREES_MAX) {
            throw new WrongParameterException("the measurement ending position must be <= " + DEGREES_MAX);
        }
        this.measurementMaxAngle = value;
    }

    private void setLampParameters(String lampParameters) {

        this.lampParameters = lampParameters == null ? "" : lampParameters;
    }

    private void setSubtractBackground(Boolean subtractBackground) {

        this.subtractBackground = subtractBackground == null ? false : subtractBackground;
    }

    private void setIntegrationTime(Integer integrationTime) throws WrongParameterException {

        if (integrationTime == null) {
            throw new WrongParameterException("integration time is not set");
        }

        boolean isAllowed = false;
        for (int allowedTime : allowedIntegrationTimes) {
            if (integrationTime.equals(allowedTime)) {
                isAllowed = true;
                break;
            }
        }
        if (isAllowed == false) throw new WrongParameterException("Wrong integration time.");
        this.integrationTime = integrationTime;
    }

    private void setMinWaveLengthToSave(String minWaveLengthToSave) throws WrongParameterException {

        Integer value;
        if (minWaveLengthToSave == null) {
            throw new WrongParameterException("minimal wavelength to save is not set");
        }
        try {
            value = Integer.parseInt(minWaveLengthToSave);
        } catch (NumberFormatException e) {
            throw new WrongParameterException("minimal wavelength to save is in wrong format");
        }
        if (value < 200) {
            throw new WrongParameterException("minimal wavelength to save must be >= 200 nm");
        }
        if (value > 850) {
            throw new WrongParameterException("minimal wavelength to save must be <= 850 nm");
        }
        this.minWaveLengthToSave = value;
    }

    private void setMaxWaveLengthToSave(String maxWaveLengthToSave) throws WrongParameterException {

        Integer value;
        if (maxWaveLengthToSave == null) {
            throw new WrongParameterException("maximal wavelength to save is not set");
        }
        try {
            value = Integer.parseInt(maxWaveLengthToSave);
        } catch (NumberFormatException e) {
            throw new WrongParameterException("maximal wavelength to save is in wrong format");
        }
        if (value < 200) {
            throw new WrongParameterException("maximal wavelength to save must be >= 200 nm");
        }
        if (value > 850) {
            throw new WrongParameterException("maximal wavelength to save must be <= 850 nm");
        }
        this.maxWaveLengthToSave = value;
    }

    private void setPulseToAngleRatio(Double pulseToAngleRatio) throws WrongParameterException {

        if (pulseToAngleRatio == null) {
            throw new WrongParameterException("pulse to angle ratio cannot be null");
        }
        this.pulseToAngleRatio = pulseToAngleRatio;
    }

    private void setComment(String comment) {

        this.comment = comment == null ? "" : comment;
    }

    private void setIsAvereageMode(Boolean isAvereageMode) {

        this.isAvereageMode = isAvereageMode == null ? false : isAvereageMode;
    }

    public void setStepSize(String stepSize) throws WrongParameterException {

        int value;

        if (stepSize == null) {
            throw new WrongParameterException("stepsize cannot be null");
        }
        try {
            value = Integer.valueOf(stepSize);
        } catch (NumberFormatException ex) {
            throw new WrongParameterException("wrong format for step size");
        }
        if (value < 1) {
            throw new WrongParameterException("the number of impulses in one step must be >= 1");
        }
        if (value > 400) {
            throw new WrongParameterException("the number of impulses in one step must be <= 400");
        }
        this.stepSize = value;
    }

    public void setBackground(double[] background) {

        this.background = background;
    }


    //-----------getters------------------------------------------------------------------------
    public Double getCalibrationMinAngle() {

        return calibrationMinAngle;
    }

    public Double getCalibrationMaxAngle() {

        return calibrationMaxAngle;
    }

    public Boolean getIsAvereageMode() {

        return isAvereageMode;
    }

    public Integer getNumberOfScansToAverage() {

        return numberOfScansToAverage;
    }

    public String getAngleUnits() {

        return angleUnits;
    }

    public Double getMeasurementMinAngle() {

        return measurementMinAngle;
    }

    public Double getMeasurementMaxAngle() {

        return measurementMaxAngle;
    }

    public String getLampParameters() {

        return lampParameters;
    }

    public Boolean getSubtractBackground() {

        return subtractBackground;
    }

    public Integer getIntegrationTime() {

        return integrationTime;
    }

    public Integer getMinWaveLengthToSave() {

        return minWaveLengthToSave;
    }

    public Integer getMaxWaveLengthToSave() {

        return maxWaveLengthToSave;
    }

    public Double getPulseToAngleRatio() {

        return pulseToAngleRatio;
    }

    public String getComment() {

        return comment;
    }

    public int[] getAllowedIntegrationTimes() {

        return allowedIntegrationTimes;
    }

    public int getPulsesSinceCalibrationStart() {

        return pulsesSinceCalibrationStart;
    }

    public int getStepSize() {

        return stepSize;
    }

    public double[] getBackground() {

        return background;
    }

}
