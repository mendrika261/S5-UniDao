package mg.uniDao.core.sql;

import mg.uniDao.core.Database;
import mg.uniDao.core.Service;
import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public interface GenericSqlDatabaseInterface extends Database {
    void prepareStatement(PreparedStatement preparedStatement, HashMap<Field, Object> attributes)
            throws IllegalAccessException, InvocationTargetException, SQLException;

    void execute(Service service, String query, HashMap<Field, Object> parameters) throws DaoException;
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
    void dropColumnUnique(Service service, String collectionName, String columnName) throws DaoException;
    void setColumnUnique(Service service, String collectionName, String columnName, boolean unique)
            throws DaoException, DatabaseException;
    void setUnique(Service service, String collectionName, String[] columnName)
            throws DaoException, DatabaseException;
    void addForeignKey(Service service, String collectionName, String columnName, String referenceCollection,
                              String referenceColumn) throws DaoException, DatabaseException;
    void dropForeignKey(Service service, String collectionName, String columnName) throws DaoException;
}
