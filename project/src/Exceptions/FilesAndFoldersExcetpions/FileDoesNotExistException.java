package Exceptions.FilesAndFoldersExcetpions;

/**
 * Exception is thrown when the target file does not exists
 */


public class FileDoesNotExistException extends Exception {

    public FileDoesNotExistException(String message) {
        super(message);
    }
}
