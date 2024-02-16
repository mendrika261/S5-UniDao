package mg.uniDao.exception;

public class DaoException extends GenericException {
    public DaoException(String message) {
        super("(DAO)\n-> " + message);
    }
}
