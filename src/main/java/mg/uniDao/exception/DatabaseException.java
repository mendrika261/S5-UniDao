package mg.uniDao.exception;

import mg.uniDao.log.GeneralLog;

/**
 * DatabaseException is thrown when an error occurs during the execution of a database operation
 */
public class DatabaseException extends RuntimeException {
    private String personalizedMessage;
    public DatabaseException(String message) {
        setPersonalizedMessage(message);
        setStackTrace(new StackTraceElement[0]);
    }

    @Override
    public String toString() {
        GeneralLog.printError(getPersonalizedMessage());
        return "";
    }

    public String getPersonalizedMessage() {
        return personalizedMessage;
    }

    public void setPersonalizedMessage(String personalizedMessage) {
        this.personalizedMessage = personalizedMessage;
    }
}
