package mg.uniDao.core;

import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;

import java.sql.Connection;
import java.util.List;

public class GenericDao {
    public void save(Service service) throws DatabaseException, DaoException {
        service.getDatabase().create(service, getClass().getSimpleName().toLowerCase(), this);
    }

    public static <T> List<T> readAllObject(Connection connection) {
        return null;
    }

    public static <T> T readObject(Connection connection) {
        return null;
    }

    public void updateObject(Connection connection) {
    }
}
