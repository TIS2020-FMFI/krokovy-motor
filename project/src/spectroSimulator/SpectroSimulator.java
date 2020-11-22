package spectroSimulator;

import java.util.Random;

public class SpectroSimulator {


    Integer minWaveLength;
    Integer maxWaveLength;

    public Integer  minInterval;
    public Integer  maxInterval;

    public SpectroSimulator(Integer minWaveLength, Integer maxWaveLength) {
        this.minWaveLength = minWaveLength;
        this.maxWaveLength = maxWaveLength;
    }

    public double[] getSpectrum(){ //intenzity
        Random rnd = new Random();
        double[] res = new double[maxWaveLength - minWaveLength];

        if(rnd.nextBoolean() == true){
            for (int i = 0; i < res.length; i++) {
                res[i] = -10 + (10 - (-10)) * rnd.nextDouble();
            }
        } else {
            for (int i = 0; i < res.length; i++) {
                res[i] = 0 + (500 + 0) * rnd.nextDouble();
            }
        }



        return res;
    }

    public double[] getWaveLengths(){
        double[] res = new double[maxWaveLength - minWaveLength];
        for (int i = 0; i < res.length; i++) {
            res[i] = minWaveLength + i;
        }

        return res;
    }
//
//    public static void main(String[] args) {
//        spectroSimulator.SpectroSimulator ss = new spectroSimulator.SpectroSimulator(50, 200, 850);
//        double d[] = ss.getSpectrum();
//        double d2[] = ss.getWaveLengths();
//        for (int i = 0; i < d.length; i++) {
//            System.out.print("("+d[i]);
//            System.out.print(", " + d2[i] + ")");
//        }
//    }
}
