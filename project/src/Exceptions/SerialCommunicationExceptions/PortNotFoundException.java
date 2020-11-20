package Exceptions.SerialCommunicationExceptions;

public class PortNotFoundException extends Exception {

    public PortNotFoundException(String message) {
        super(message);
    }
}
