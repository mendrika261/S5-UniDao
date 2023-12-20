package mg.uniDao.core;

import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;

import java.util.List;

public interface Database {
     void loadDriver() throws DatabaseException;
     Service connect(boolean transaction) throws DatabaseException;
     Service connect() throws DatabaseException;
     void create(Service service, String collectionName, Object object) throws DaoException;
     <T> List<T> readAll(Service service, String collectionName, Class<T> className, int page, int limit) throws DaoException;
     <T> T read(Service service, String collectionName, Class<?> className);
     void update(Service service, Object object);
}
