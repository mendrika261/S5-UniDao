package mg.uniDao.core;

import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;
import mg.uniDao.log.GeneralLog;

import javax.xml.crypto.Data;

public class Service {
    private Object access;
    private boolean transactional;
    private boolean closed;
    private Database database;
    static int NB_CONNECTION;

    public Service(Database database, Object access, boolean transactional) throws DaoException {
        setDatabase(database);
        setAccess(access);
        setTransactional(transactional);
        NB_CONNECTION++;
        GeneralLog.printWarning(NB_CONNECTION + " connection(s) opened");
    }

    public Service(Database database, Object access) throws DaoException {
        new Service(database, access, true);
    }

    public void commit() {
        try {
            getAccess().getClass().getMethod("commit").invoke(getAccess());
        } catch (Exception e) {
            try {
                getAccess().getClass().getMethod("rollback").invoke(getAccess());
            } catch (Exception ignored2) {}
            throw new DatabaseException("Cannot commit changes! " + e.getMessage());
        }
    }

    private void close() {
        try {
            getAccess().getClass().getMethod("close").invoke(getAccess());
        } catch (Exception ignored) {}
    }
    public void endConnection() {
        commit();
        close();
        setClosed(true);
        NB_CONNECTION--;
        GeneralLog.printWarning(NB_CONNECTION + " connection(s) opened");
    }

    public Object getAccess() throws DaoException {
        if(isClosed())
            throw new DaoException("This service is already closed, set transactional true for multiple usage\n" +
                    "Important! Note that the first query was committed");
        return access;
    }

    public void setAccess(Object access) throws DaoException {
        if(access == null)
            throw new DaoException("Cannot have access to database with the given information!");
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
