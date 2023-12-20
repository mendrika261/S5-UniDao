package mg.uniDao.core;

import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;

public class Service {
    private Object access;
    private boolean transactional=true;
    private boolean closed;

    public Service(Object access, boolean transactional) throws DatabaseException {
        setAccess(access);
        setTransactional(transactional);
    }

    public Service(Object access) throws DatabaseException {
        setAccess(access);
    }

    public void commit() throws DaoException {
        try {
            getAccess().getClass().getMethod("commit").invoke(getAccess());
        } catch (Exception ignored) {
            try {
                getAccess().getClass().getMethod("rollback").invoke(getAccess());
            } catch (Exception ignored2) {}
            throw new DaoException("Cannot commit changes");
        }
    }

    public void close() {
        try {
            getAccess().getClass().getMethod("close").invoke(getAccess());
        } catch (Exception ignored) {}
    }
    public void endConnection() throws DaoException {
        commit();
        close();
        setClosed(true);
    }

    public Object getAccess() throws DaoException {
        if(isClosed())
            throw new DaoException("This service is already closed, set transactional true for multiple usage\n" +
                    "Important! Note that the first query was committed");
        return access;
    }

    public void setAccess(Object access) throws DatabaseException {
        if(access == null)
            throw new DatabaseException("Cannot have access to database with the given access");
        this.access = access;
    }

    public boolean isTransactional() {
        return transactional;
    }

    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
