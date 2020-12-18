package Exceptions.SerialCommunicationExceptions;

/**
 * Exception is thrown when the port for picaxe chip is not found
 */

public class PortNotFoundException extends Exception {

    public PortNotFoundException(String message) {

        super(message);
    }
}
