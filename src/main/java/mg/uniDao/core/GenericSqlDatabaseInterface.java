package mg.uniDao.core;

import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public interface GenericSqlDatabaseInterface extends Database {
    void prepareStatement(PreparedStatement preparedStatement, HashMap<String, Object> attributes)
            throws IllegalAccessException, InvocationTargetException, SQLException;

    void execute(Service service, String query, HashMap<String, Object> parameters) throws DaoException;
    void execute(Service service, String query) throws DaoException;

    String getNextSequenceValue(Service service, String sequenceName) throws DaoException;
    void dropCollection(Service service, String collectionName) throws DaoException;
    void dropPrimaryKey(Service service, String collectionName) throws DaoException;
    void addPrimaryKey(Service service, String collectionName, List<String> primaryKeyColumns) throws DaoException;
    void alterColumnType(Service service, String collectionName, String columnName, String columnType)
            throws DaoException, DatabaseException;
    void setColumnNullable(Service service, String collectionName, String columnName, boolean nullable)
            throws DaoException, DatabaseException;
    void createSequence(Service service, String sequenceName) throws DaoException;
    void setColumnUnique(Service service, String collectionName, String columnName, boolean unique)
            throws DaoException, DatabaseException;
}
