package Exceptions.FilesAndFoldersExcetpions;

/**
 * Exception is thrown when the target folder does not exists
 */


public class MissingFolderException extends Exception{

    public MissingFolderException(String message) {
        super(message);
    }
}
