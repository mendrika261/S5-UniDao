package mg.uniDao.exception;

import mg.uniDao.log.GeneralLog;

public class DaoException extends Exception {
    private String personalizedMessage;
    public DaoException(String message) {
        super();
        setPersonalizedMessage(message);
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
