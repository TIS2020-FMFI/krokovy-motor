package Exceptions.FilesAndFoldersExcetpions;

/**
 * Exception is thrown when the target file already exists
 */

public class FileAlreadyExistsException extends Exception {
    public FileAlreadyExistsException(String message) {
        super(message);
    }
}
