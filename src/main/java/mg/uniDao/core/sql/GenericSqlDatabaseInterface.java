package mg.uniDao.core.sql;

import mg.uniDao.core.Database;
import mg.uniDao.core.Service;
import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;

public interface GenericSqlDatabaseInterface extends Database {
    void prepareStatement(PreparedStatement preparedStatement, LinkedHashMap<String, Object> attributes)
            throws IllegalAccessException, InvocationTargetException, SQLException;

    void execute(Service service, String query, LinkedHashMap<String, Object> parameters) throws DaoException;

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


    String createSQL(String collectionName, LinkedHashMap<String, Object> attributes);
    String findListWithLimitSQL(String collectionName, String extraCondition,
                                                   List<Joiner> joiners);
    String findSQL(String collectionName, LinkedHashMap<String, Object> conditions,
                                      String extraCondition, List<Joiner> joiners);
    String updateSQL(String collectionName, LinkedHashMap<String, Object> attributes,
                     LinkedHashMap<String, Object> conditions, String extraCondition);
    String deleteSQL(String collectionName, LinkedHashMap<String, Object> conditions, String extraCondition);
    String getNextSequenceValueSql(String sequenceName);
    String createCollectionSQL(String collectionName);
    String addColumnSQL(String collectionName, String columnName, String columnType);
    String addForeignKeySQL(String collectionName, String columnName, String referenceCollection,
                                      String referenceColumn);
    String dropForeignKeySQL(String collectionName, String columnName);
    String dropCollectionSQL(String collectionName);
    String addPrimaryKeySQL(String collectionName, List<String> primaryKeyColumns);
    String dropPrimaryKeySQL(String collectionName);
    String alterColumnTypeSQL(String collectionName, String columnName, String columnType);
    String setColumnNullableSQL(String collectionName, String columnName, boolean nullable);
    String createSequenceSQL(String sequenceName);
    String addColumnUniqueSQL(String collectionName, String columnName);
    String dropColumnUniqueSQL(String collectionName, String columnName);
    String addUniqueSQL(String collectionName, String[] columnName);
    String dropUniqueSQL(String collectionName);
}
