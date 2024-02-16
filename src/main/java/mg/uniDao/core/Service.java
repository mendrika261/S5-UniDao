package mg.uniDao.core;

import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;

import javax.xml.crypto.Data;

public class Service {
    private Object access;
    private boolean transactional;
    private boolean closed;
    private Database database;

    public Service(Database database, Object access, boolean transactional) throws DatabaseException {
        setDatabase(database);
        setAccess(access);
        setTransactional(transactional);
    }

    public Service(Database database, Object access) throws DatabaseException {
        new Service(database, access, true);
    }

    public void commit() {
        try {
            getAccess().getClass().getMethod("commit").invoke(getAccess());
        } catch (Exception e) {
            try {
                getAccess().getClass().getMethod("rollback").invoke(getAccess());
            } catch (Exception ignored2) {}
            throw new RuntimeException("Cannot commit changes! " + e.getMessage());
        }
    }

    private void close() {
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

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }
}
