package measurement;

import java.util.*;

public class MinValues {
    private final Map<Double,Map<Double, Double>> auxiliaryTable;
    private final ArrayList<Double> minIntensityInitialValues;
    private final ArrayList<Double> waveLengthsInitialValues;

    public MinValues(SeriesOfMeasurements seriesOfMeasurements){
        this.auxiliaryTable = new TreeMap();
        this.minIntensityInitialValues = new ArrayList();
        this.waveLengthsInitialValues = new ArrayList();

        for(Measurement measurement : seriesOfMeasurements.measurements){
            fillAuxiliaryTable(
                    measurement.getWavelengths(),
                    measurement.getSpectrumValues(),
                    measurement.getAngle()
            );
        }
    }

    private void fillAuxiliaryTable(double[] waveLengths, double[] values, double angle){
        for (int index = 0; index < waveLengths.length; index++) {
            if(!auxiliaryTable.isEmpty()){
                if(auxiliaryTable.get(waveLengths[index]) != null){
                    Map intensityToAngleMap = auxiliaryTable.get(waveLengths[index]);
                    intensityToAngleMap.put(angle,values[index]);
                    auxiliaryTable.put(waveLengths[index],intensityToAngleMap);
                    continue;
                }
            }
            Map<Double, Double> intesityToAngleMap = new TreeMap();
            intesityToAngleMap.put(angle,values[index]);
            auxiliaryTable.put(waveLengths[index],intesityToAngleMap);
        }
    }

    private void findMinValues(){
        for (Double wl : auxiliaryTable.keySet()) findMinimum(wl);
    }

    private void findMinimum(Double wavelength){
        Map<Double, Double> intensityToAngleMap = auxiliaryTable.get(wavelength);

        Set<Double> anglesSet = intensityToAngleMap.keySet();
        double[] anglesArray = new double[anglesSet.size()];
        int i = 0;
        for(Double element : anglesSet) anglesArray[i++] = element;

        Collection<Double> intensityCollection = intensityToAngleMap.values();
        double[] intensityArray = new double[intensityCollection.size()];
        int j = 0;
        for(Double element : intensityCollection) intensityArray[j++] = element;

        findPolynom(anglesArray,intensityArray);
    }

    private void findPolynom(double[] anglesArray, double[] intensityArray){
        PolynomialRegression regression = new PolynomialRegression(anglesArray, intensityArray, 2);
        minIntensityInitialValues.add(getQuadraticFunctionMinimum(regression.beta(2),regression.beta(1),regression.beta(0)));
    }

    static double getQuadraticFunctionMinimum(double a, double b, double c) {
        return (c * 1.0 - (b * b / (4.0 * a)));
    }

    public double[] getWaveLengthsInitialValues(){
        waveLengthsInitialValues.addAll(auxiliaryTable.keySet());
        return convertToDoubleArray(waveLengthsInitialValues);
    }

    public double[] getMinIntensityInitialValues(){
        findMinValues();
        return convertToDoubleArray(minIntensityInitialValues);
    }

    private double[] convertToDoubleArray(ArrayList<Double> arrayList){
        double[] array = new double[arrayList.size()];
        int i = 0;
        for(Double element : arrayList) array[i++] = element;
        return array;
    }
}