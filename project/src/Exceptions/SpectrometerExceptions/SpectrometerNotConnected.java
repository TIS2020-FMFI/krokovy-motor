package Exceptions.SpectrometerExceptions;

/**
 * Exception is thrown when the spectrometer is not connected
 */

public class SpectrometerNotConnected extends Exception {
    public SpectrometerNotConnected(String message) {
        super(message);
    }
}
