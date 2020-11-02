package spectrometer;

import com.oceanoptics.omnidriver.api.wrapper.Wrapper;

public class SpectrometerWrapper {

    private static final Wrapper wrapper = new Wrapper();

    private SpectrometerWrapper() {
    }

    public static Wrapper getInstance() {
        return wrapper;
    }
}
