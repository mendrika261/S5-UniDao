package mg.uniDao.exception;

import mg.uniDao.log.GeneralLog;

public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        setStackTrace(new StackTraceElement[0]);
        GeneralLog.printError(message);
    }

    @Override
    public String toString() {
        return "";
    }
}
