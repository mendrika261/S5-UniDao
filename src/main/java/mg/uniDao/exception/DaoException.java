package mg.uniDao.exception;

import mg.uniDao.core.GenericDao;

public class DaoException extends GenericException {
    public DaoException(String message) {
        super("(DAO)\n-> " + message);
    }
}
