package serialCommunication;

import Exceptions.SpectrometerExceptions.SpectrometerNotConnected;
import com.oceanoptics.omnidriver.api.wrapper.Wrapper;

public class Spectrometer {

    private Wrapper wrapper;

    public Spectrometer(Wrapper wrapper) {
        this.wrapper = wrapper;
    }

    public void checkConnection() throws SpectrometerNotConnected {
        if(wrapper == null){
            throw new SpectrometerNotConnected("Spectrometer is not connected");
        }

        int numberOfSpectrometers;
        try{
            numberOfSpectrometers = wrapper.openAllSpectrometers();
        } catch (java.lang.ExceptionInInitializerError | java.lang.NoClassDefFoundError e){
            throw new SpectrometerNotConnected("Spectrometer is not connected");
        }

        if(numberOfSpectrometers == -1){ //nejaka specialna chyba
            throw new SpectrometerNotConnected(wrapper.lastException.getMessage());
        }
        if(numberOfSpectrometers == 0){
            throw new SpectrometerNotConnected("Spectrometer is not connected");
        }
        if(numberOfSpectrometers > 1){
            throw new SpectrometerNotConnected("Multiple spectrometers are connected");
        }
    }
}