package Exceptions.SerialCommunicationExceptions;

/**
 * Exception is thrown when the current angle is not computed yet
 */

public class UnknownCurrentAngleException extends Exception {

    public UnknownCurrentAngleException(String message) {

        super(message);
    }
}
