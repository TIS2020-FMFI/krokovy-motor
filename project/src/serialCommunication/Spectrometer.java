package serialCommunication;

import Exceptions.SpectrometerExceptions.SpectrometerNotConnected;
import com.oceanoptics.omnidriver.api.wrapper.Wrapper;

public class Spectrometer {

    private Wrapper wrapper;

    /**
     * @param wrapper an instance of the Wrapper class
     */
    public Spectrometer(Wrapper wrapper) {
        this.wrapper = wrapper;
    }

    /**
     * checks if the spectrometer is connected
     * @throws SpectrometerNotConnected
     */
    public void checkConnection() throws SpectrometerNotConnected {
        if(wrapper == null){
            throw new SpectrometerNotConnected("Spectrometer is not connected, wrapper is null");
        }

        int numberOfSpectrometers;
        try{
            numberOfSpectrometers = wrapper.openAllSpectrometers();
        } catch (java.lang.ExceptionInInitializerError | java.lang.NoClassDefFoundError e){
            throw new SpectrometerNotConnected(e.getMessage());
        }

        if(numberOfSpectrometers == -1){
            throw new SpectrometerNotConnected(wrapper.lastException.getMessage());
        }
        if(numberOfSpectrometers == 0){
            throw new SpectrometerNotConnected("Spectrometer is not connected, 0 spectrometes connected");
        }
        if(numberOfSpectrometers > 1){
            throw new SpectrometerNotConnected("Multiple spectrometers are connected. more spectrometers");
        }
    }
}
