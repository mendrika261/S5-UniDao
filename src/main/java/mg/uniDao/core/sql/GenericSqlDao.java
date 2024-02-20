package mg.uniDao.core.sql;

import mg.uniDao.annotation.Collection;

import mg.uniDao.core.Service;
import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;
import mg.uniDao.util.ObjectUtils;

import java.util.List;

public class GenericSqlDao {
    private String getCollectionName() {
        if (getClass().isAnnotationPresent(Collection.class))
            return getClass().getAnnotation(Collection.class).name();
        return getClass().getSimpleName().toLowerCase();
    }

    public void save(Service service) throws DaoException {
        service.getDatabase().create(service, getCollectionName(), this);
    }

    public <T> List<T> findList(Service service, int page, int limit, String extraCondition, String joins)
            throws DaoException {
        return service.getDatabase().findList(service, getCollectionName(),
                (Class<T>) getClass(), page, limit, extraCondition, joins);
    }

    public <T> T find(Service service, String extraCondition, String... joins) throws DaoException {
        return service.getDatabase().find(service, getCollectionName(), this, extraCondition, joins);
    }

    public void update(Service service, Object condition, String extraCondition) throws DaoException {
        service.getDatabase().update(service, getCollectionName(), condition, this, extraCondition);
    }

    public void delete(Service service, String extraCondition) throws DaoException {
        service.getDatabase().delete(service, getCollectionName(), this, extraCondition);
    }


    public void createCollection(Service service) throws DaoException, DatabaseException {
        service.getDatabase().createCollection(service, getCollectionName(), this.getClass());
    }

    @Override
    public String toString() {
        try {
            return String.valueOf(ObjectUtils.getFieldsNamesWithValuesNonNull(this));
        } catch (DaoException ignored) {}
        return null;
    }
}
