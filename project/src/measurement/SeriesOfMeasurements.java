package measurement;

import Exceptions.FilesAndFoldersExcetpions.*;
import settings.Settings;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SeriesOfMeasurements {

    List<Measurement> measurements = new ArrayList();
    String mainDirPath = "measuredData";

    public SeriesOfMeasurements()  { }

    public void save() throws ParameterIsNullException {
        if(measurements.isEmpty()) throw new ParameterIsNullException("there are no measurements to save");

        //create dir for this series
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date(System.currentTimeMillis());
        String seriesDirName = formatter.format(date);

        String seriesDirPath = mainDirPath + File.separator + seriesDirName;
        File seriesDir = new File(seriesDirPath);
        seriesDir.mkdirs();  //ak mainDir neexistuje, mkdirs() vytvori aj to

        //save config file to the created dir
        try {
            Settings.saveToFile(seriesDirPath);
        } catch (FileAlreadyExistsException e) {
            e.printStackTrace();
        } catch (MissingFolderException e) {
            e.printStackTrace();
        } catch (FileDoesNotExistException e) {
            e.printStackTrace();
        }

        //save measurements to the created dir
        for(Measurement m : measurements){
            try {
                m.saveToFile(seriesDirPath);
            } catch (MissingFolderException e) {
                e.printStackTrace();
            } catch (FileAlreadyExistsException e) {
                e.printStackTrace();
            } catch (FileDoesNotExistException e) {
                e.printStackTrace();
            }
        }
    }

    public void addMeasurement(Measurement m) throws ParameterIsNullException {
        if(m == null) throw new ParameterIsNullException("measurement cannot be null");
        measurements.add(m);
    }


    public void setMainDirPath(String mainDirPath) {
        this.mainDirPath = mainDirPath;
    }



    public static void main(String[] args) {  //test
        double[] waveLengths = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        double[] values = {100.78, 150.01, 200.8, 300.0, 50.5, 300, 222.2, 134.12, 123.10, 99.99};
        double angle = 90.123457;
        Measurement m1 = null;
        double angle2 = 90.12;
        Measurement m2 = null;
        try {
            m1 = new Measurement(values, waveLengths, angle);
            m2 = new Measurement(values, waveLengths, angle2);
            SeriesOfMeasurements series = new SeriesOfMeasurements();
            series.addMeasurement(m1);
            series.addMeasurement(m2);
            /*ConfigurationFile c = new ConfigurationFile(true, 10, "gradians", 0d,
                    120d, "wolframova halogenova lampa, 10 voltov, 10 amperov, velmi dobra lampa", false,
                    50, 200, 400, 0.5, "koment ku meraniu");
            series.setConfigurationFile(c);*/
            Settings.setStepToAngleRatio(1.0);
            series.save();
        } catch (ParameterIsNullException e) {
            System.out.println(e.getMessage());
        }
    }
}
