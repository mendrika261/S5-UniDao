package mg.uniDao.core;

import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;

import java.util.HashMap;
import java.util.List;

public interface Database {
     void loadDriver() throws DatabaseException;
     Service connect(boolean transaction) throws DatabaseException;
     Service connect() throws DatabaseException;
     void create(Service service, String collectionName, Object object) throws DaoException;
     <T> List<T> findList(Service service, String collectionName, Class<T> className, int page, int limit, String extraCondition) throws DaoException;
     <T> T find(Service service, String collectionName, Object condition, String extraCondition) throws DaoException;
     void update(Service service, String collectionName, Object condition, Object object, String extraCondition) throws DaoException;
     void delete(Service service, String collectionName, Object condition, String extraCondition) throws DaoException;
     String getNextSequenceValue(Service service, String sequenceName) throws DaoException;
}
