package mg.uniDao.exception;

import mg.uniDao.log.GeneralLog;

public class DaoException extends Exception {
    public DaoException(String message) {
        super();
        GeneralLog.printError(message);
    }
}
