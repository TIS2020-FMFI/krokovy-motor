package Exceptions.FilesAndFoldersExcetpions;

/**
 * Exception is thrown when the parameter is wrong
 */

public class WrongParameterException extends Exception {
    public WrongParameterException(String message) {
        super(message);
    }
}