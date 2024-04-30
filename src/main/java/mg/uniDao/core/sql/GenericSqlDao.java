package mg.uniDao.core.sql;

import mg.uniDao.core.Service;
import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;
import mg.uniDao.util.ObjectUtils;

import java.util.List;

/**
 * GenericSqlDao is a generic class that provides basic database operations
 *
 */
public class GenericSqlDao {
    public GenericSqlDao() {
    }

    /**
        * Saves the object to the database
        *
        * @param service the service to use
        * @throws DaoException if an error occurs during the operation
        */
    public void save(Service service) throws DaoException {
        service.getDatabase().create(service, this);
    }

    /**
        * Saves the object to the database
        *
        * @param service the service to use
        * @param condition the condition to use
        * @throws DaoException if an error occurs during the operation
        */
    public <T> List<T> getList(Service service, String condition, int page, int limit, String... joins)
            throws DaoException {
        return service.getDatabase().findList(service, (Class<T>) getClass(), condition, page, limit, joins);
    }

    /**
        * Saves the object to the database
        *
        * @param service the service to use
        * @param page the page to use
        * @param limit the limit to use
        * @param joins the joins to use
        * @throws DaoException if an error occurs during the operation
        */
    public <T> List<T> getList(Service service, int page, int limit, String... joins)
            throws DaoException {
        return service.getDatabase().findList(service, (Class<T>) getClass(), page, limit, joins);
    }

    /**
        * Saves the object to the database
        *
        * @param service the service to use
        * @param joins the joins to use
        * @throws DaoException if an error occurs during the operation
        */
    public <T> List<T> getList(Service service, String... joins) throws DaoException {
        return service.getDatabase().findList(service, (Class<T>) getClass(), joins);
    }

    /**
        * Saves the object to the database
        *
        * @param service the service to use
        * @param condition the condition to use
        * @param joins the joins to use
        * @throws DaoException if an error occurs during the operation
        */
    public <T> T get(Service service, String condition, String[] joins) throws DaoException {
        return service.getDatabase().find(service, getClass(), condition, joins);
    }

    /**
        * Saves the object to the database
        *
        * @param service the service to use
        * @param joins the joins to use
        * @throws DaoException if an error occurs during the operation
        */
    public <T> T get(Service service, String... joins) throws DaoException {
        return service.getDatabase().find(service, this, joins);
    }

    /**
        * Saves the object to the database
        *
        * @param service the service to use
        * @param id the id to use
        * @param joins the joins to use
        * @throws DaoException if an error occurs during the operation
        */
    public <T> T getById(Service service, Object id, String... joins) throws DaoException {
        return service.getDatabase().findById(service, getClass(), id, joins);
    }

    /**
        * Check if the object exists in the database using all fields as condition
        * @param service the service to use
        * @return true if the object exists in the database, false otherwise
        * @throws DaoException if an error occurs during the operation
        */
    public boolean exists(Service service) throws DaoException {
        return service.getDatabase().exists(service, this);
    }

    /**
        * Check if the object exists in the database by its id
        * @param service the service to use
        * @return true if the object exists in the database, false otherwise
        * @throws DaoException if an error occurs during the operation
        */
    public boolean existsById(Service service) throws DaoException {
        return service.getDatabase().existsById(service, getClass(), ObjectUtils.getId(this));
    }

    /**
        * Updates the object in the database
        * @param service the service to use
        * @throws DaoException if an error occurs during the operation
        */
    public void update(Service service) throws DaoException {
        service.getDatabase().updateById(service, this, ObjectUtils.getId(this));
    }

    /**
        * Updates the object in the database
        * @param service the service to use
        * @param condition the condition to use
        * @throws DaoException if an error occurs during the operation
        */
    public void update(Service service, String condition) throws DaoException {
        service.getDatabase().update(service, this, condition);
    }

    /**
        * Updates the object in the database
        * @param service the service to use
        * @param conditionObject the conditionObject to use
        * @throws DaoException if an error occurs during the operation
        */
    public void update(Service service, Object conditionObject) throws DaoException {
        service.getDatabase().update(service, this, conditionObject);
    }

    /**
        * Deletes the object from the database
        * @param service the service to use
        * @throws DaoException if an error occurs during the operation
        */
    public void delete(Service service) throws DaoException {
        service.getDatabase().deleteById(service, getClass(), ObjectUtils.getId(this));
    }

    /**
        * Deletes the object from the database
        * @param service the service to use
        * @param condition the condition to use
        * @throws DaoException if an error occurs during the operation
        */
    public void delete(Service service, String condition) throws DaoException {
        service.getDatabase().delete(service, getClass(), condition);
    }

    /**
        * Creates the collection in the database
        * @param service the service to use
        * @throws DaoException if an error occurs during the operation
        */
    public void createCollection(Service service) throws DaoException, DatabaseException {
        service.getDatabase().createCollection(service, this.getClass());
    }

    /**
        * Saves the object to the database if it does not exist, updates it otherwise
        * @param service the service to use
        * @throws DaoException if an error occurs during the operation
        */
    public void saveOrUpdate(Service service) throws DaoException {
        service.getDatabase().createOrUpdate(service, this);
    }

    @Override
    public String toString() {
        try {
            return String.valueOf(ObjectUtils.getFieldsNamesWithValuesNonNull(this));
        } catch (DaoException ignored) {}
        return null;
    }
}
