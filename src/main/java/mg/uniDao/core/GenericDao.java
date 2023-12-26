package mg.uniDao.core;

import mg.uniDao.exception.DaoException;

import java.sql.Connection;
import java.util.List;

public class GenericDao {
    public void save(Service service) throws DaoException {
        service.getDatabase().create(service, getClass().getSimpleName().toLowerCase(), this);
    }

    public static <T> List<T> findList(Service service, Class<T> castType, int page, int limit, String extraCondition)
            throws DaoException {
        return service.getDatabase().findList(service, castType.getSimpleName().toLowerCase(), castType, page, limit, extraCondition);
    }

    public <T> T find(Service service, String extraCondition) throws DaoException {
        return service.getDatabase().find(service, getClass().getSimpleName().toLowerCase(), this, extraCondition);
    }

    public void update(Connection connection) {
    }
}
