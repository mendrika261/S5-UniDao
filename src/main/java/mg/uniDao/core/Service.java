package mg.uniDao.core;

import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;
import mg.uniDao.log.GeneralLog;

import javax.xml.crypto.Data;
import java.time.Instant;

/**
 * Service class is used to manage the connection to the database
 */
public class Service {
    /**
     * The access to the database, e.g.: Connection if using JDBC
     */
    private Object access;
    /**
     * The transactional state of the connection
     */
    private boolean transactional;
    /**
     * The state of the connection
     */
    private boolean closed;
    /**
     * The database used by the connection
     */
    private Database database;
    /**
     * The time of creation of the connection
     */
    private final long creationTime = System.currentTimeMillis();
    /**
     * The total number of connections opened
     */
    static int NB_CONNECTION;

    public Service(Database database, Object access, boolean transactional) throws DaoException {
        setDatabase(database);
        setAccess(access);
        setTransactional(transactional);
        NB_CONNECTION++;
        GeneralLog.printInfo(NB_CONNECTION + " connection(s) opened");
    }

    public Service(Database database, Object access) throws DaoException {
        new Service(database, access, true);
    }

    /**
     * Commit the changes made to the database
     *
     * @throws DatabaseException if the changes cannot be committed
     */
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

    /**
     * Close the connection to the database
     */
    private void close() {
        try {
            getAccess().getClass().getMethod("close").invoke(getAccess());
        } catch (Exception ignored) {}
    }

    /**
     * End the connection to the database
     */
    public void endConnection() {
        commit();
        close();
        setClosed(true);
        NB_CONNECTION--;
        final long lifeDuration = System.currentTimeMillis() - creationTime;
        GeneralLog.printInfo("1 connection closed after " + lifeDuration + "ms, " +
                NB_CONNECTION + " connection(s) opened");
    }


    /**
     * Get the access to the database, e.g.: Connection if using JDBC
     *
     * @return the access to the database
     * @throws DaoException if the connection is closed
     */
    public Object getAccess() throws DaoException {
        if(isClosed())
            throw new DaoException("This service is already closed, set transactional true for multiple usage\n" +
                    "Important! Note that the first query was committed");
        return access;
    }

    /**
     * Set the access to the database, e.g.: Connection if using JDBC
     *
     * @param access the access to the database
     * @throws DaoException if the access is null
     */
    public void setAccess(Object access) throws DaoException {
        if(access == null)
            throw new DaoException("Cannot have access to database with the given information!");
        this.access = access;
    }

    /**
     * Get the transactional state of the connection
     *
     * @return the transactional state of the connection
     */
    public boolean isTransactional() {
        return transactional;
    }

    /**
     * Set the transactional state of the connection
     *
     * @param transactional the transactional state of the connection
     */
    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

    /**
     * Get the state of the connection
     *
     * @return the state of the connection
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Set the state of the connection
     *
     * @param closed the state of the connection
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    /**
     * Get the database used by the connection
     *
     * @return the database used by the connection
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Set the database used by the connection
     *
     * @param database the database used by the connection
     */
    public void setDatabase(Database database) {
        this.database = database;
    }
}
