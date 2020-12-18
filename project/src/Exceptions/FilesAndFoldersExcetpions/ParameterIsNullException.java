package Exceptions.FilesAndFoldersExcetpions;

/**
 * Exception is thrown when the parameter is null
 */


public class ParameterIsNullException extends Exception {
    public ParameterIsNullException(String message) {
        super(message);
    }
}
