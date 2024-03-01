package mg.uniDao.exception;

import mg.uniDao.log.GeneralLog;

public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
        setStackTrace(new StackTraceElement[0]);
    }

    @Override
    public String toString() {
        GeneralLog.printError(getMessage());
        return "";
    }
}
