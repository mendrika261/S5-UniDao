package mg.uniDao.core;

import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;

import java.util.List;

public interface Database<T> {
     void loadDriver() throws DatabaseException;
     Service connect(boolean transaction) throws DatabaseException;
     void createObject(Service service, String collectionName, Object object) throws DaoException;
     List<T> readAllObject(Service service, String collectionName, Class<?> className);
     T readObject(Service service, String collectionName, Class<?> className);
     void updateObject(Service service, Object object);
}
