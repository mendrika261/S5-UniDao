package mg.uniDao.exception;

public class GenericException extends Exception {
public GenericException(String message) {
        super("\nMyDao error: " + message);
    }
}