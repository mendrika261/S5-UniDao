package mg.uniDao.core.sql;

import mg.uniDao.core.Service;
import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;
import mg.uniDao.util.ObjectUtils;

import java.util.List;

public class GenericSqlDao {
    public GenericSqlDao() {
    }

    public void save(Service service) throws DaoException {
        service.getDatabase().create(service, this);
    }

    public <T> List<T> getList(Service service, String condition, int page, int limit, String... joins)
            throws DaoException {
        return service.getDatabase().findList(service, (Class<T>) getClass(), condition, page, limit, joins);
    }

    public <T> List<T> getList(Service service, int page, int limit, String... joins)
            throws DaoException {
        return service.getDatabase().findList(service, (Class<T>) getClass(), page, limit, joins);
    }

    public <T> List<T> getList(Service service, String... joins) throws DaoException {
        return service.getDatabase().findList(service, (Class<T>) getClass(), joins);
    }

    public <T> T get(Service service, String condition, String[] joins) throws DaoException {
        return service.getDatabase().find(service, getClass(), condition, joins);
    }

    public <T> T get(Service service, String... joins) throws DaoException {
        return service.getDatabase().find(service, this, joins);
    }

    public <T> T getById(Service service, String id, String... joins) throws DaoException {
        return service.getDatabase().findById(service, getClass(), id, joins);
    }

    public boolean exists(Service service) throws DaoException {
        return service.getDatabase().exists(service, this);
    }

    public boolean existsById(Service service) throws DaoException {
        return service.getDatabase().existsById(service, getClass(), ObjectUtils.getId(this));
    }

    public void update(Service service) throws DaoException {
        service.getDatabase().updateById(service, this, ObjectUtils.getId(this));
    }

    public void update(Service service, String condition) throws DaoException {
        service.getDatabase().update(service, this, condition);
    }

    public void update(Service service, Object conditionObject) throws DaoException {
        service.getDatabase().update(service, this, conditionObject);
    }

    public void delete(Service service) throws DaoException {
        service.getDatabase().deleteById(service, getClass(), ObjectUtils.getId(this));
    }

    public void delete(Service service, String condition) throws DaoException {
        service.getDatabase().delete(service, getClass(), condition);
    }

    public void delete(Service service, Object conditionObject) throws DaoException {
        service.getDatabase().delete(service, conditionObject);
    }

    public void createCollection(Service service) throws DaoException, DatabaseException {
        service.getDatabase().createCollection(service, this.getClass());
    }

    @Override
    public String toString() {
        try {
            return String.valueOf(ObjectUtils.getFieldsNamesWithValuesNonNull(this));
        } catch (DaoException ignored) {}
        return null;
    }
}
