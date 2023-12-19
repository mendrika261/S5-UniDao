package mg.uniDao.exception;

public class DatabaseException extends GenericException {
    public DatabaseException(String message) {
        super("(Database)\n-> " + message);
    }
}
