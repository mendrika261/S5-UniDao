package mg.uniDao.core;

import mg.uniDao.annotation.Collection;
import mg.uniDao.exception.DaoException;

import java.sql.Connection;
import java.util.List;

public class GenericDao {
    private String getCollectionName() {
        if (getClass().isAnnotationPresent(Collection.class))
            return getClass().getAnnotation(Collection.class).name();
        return getClass().getSimpleName().toLowerCase();
    }

    public void save(Service service) throws DaoException {
        service.getDatabase().create(service, getCollectionName(), this);
    }

    public <T> List<T> findList(Service service, int page, int limit, String extraCondition)
            throws DaoException {
        return service.getDatabase().findList(service, getCollectionName(), (Class<T>) getClass(), page, limit, extraCondition);
    }

    public <T> T find(Service service, String extraCondition) throws DaoException {
        return service.getDatabase().find(service, getCollectionName(), this, extraCondition);
    }

    public void update(Service service, Object condition, String extraCondition) throws DaoException {
        service.getDatabase().update(service, getCollectionName(), condition, this, extraCondition);
    }

    public void delete(Service service, String extraCondition) throws DaoException {
        service.getDatabase().delete(service, getCollectionName(), this, extraCondition);
    }
}
