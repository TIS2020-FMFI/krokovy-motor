package Exceptions.SerialCommunicationExceptions;

/**
 * Exception is thrown when the picaxe chip is not connected
 */

public class PicaxeConnectionErrorException extends Exception {

    public PicaxeConnectionErrorException(String message) {

        super(message);
    }
}
