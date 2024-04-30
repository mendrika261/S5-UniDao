package mg.uniDao.core;

import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * UniDao library interface to be implemented by the database providers.
 */
public interface Database {
     /**
      * Loads the database driver.
      *
      * @throws DaoException if the driver loading fails
      *
      */
     void loadDriver(String configName) throws DaoException;

     /**
      * Connects to the database.
      *
      * @param transaction if true, starts a transaction
      * @return a {@link Service}
      * @throws DaoException if the connection fails
      */
     Service connect( String configName, boolean transaction) throws DaoException;

     /**
      * Connects to the database, transaction is true by default.
      *
      * @return a {@link Service}
      * @throws DaoException if the connection fails
      */
     Service connect(String configName) throws DaoException;
     Service connect(boolean transaction) throws DaoException;
     Service connect() throws DaoException;

     /**
      * Persist an object in the database.
      *
      * @param service the service to use
      * @param object the object to be persisted
      * @throws DaoException if the collection creation fails
      */
     void create(Service service, Object object) throws DaoException;

     /**
      * Checks if a collection exists in the database.
      *
      * @param service the service to use
      * @param className the class of the object to be checked
      * @param condition the condition of the object to be checked, e.g. "id = '123'"
      * @return true if the collection exists, false otherwise
      * @throws DaoException if the collection existence check fails
      */
     boolean exists(Service service, Class<?> className, String condition) throws DaoException;

     /**
      * Checks if a collection exists in the database.
      *
      * @param service the service to use
      * @param conditionObject the object to be checked, primary type is not compatible
      * @return true if the collection exists, false otherwise
      * @throws DaoException if the collection existence check fails
      */
     boolean exists(Service service, Object conditionObject) throws DaoException;

     /**
      * Checks if a collection exists in the database by its id.
      *
      * @param service the service to use
      * @param className the class of the object to be checked
      * @param id the id of the object to be checked
      * @return true if the collection exists, false otherwise
      * @throws DaoException if the collection existence check fails
      */
     boolean existsById(Service service, Class<?> className, Object id) throws DaoException;

     /**
      * Create an object in the database if it does not exist, update it if it does.
      *
      * @param service the service to use
      * @param newObject the object to be created or updated
      * @throws DaoException if the collection creation or update fails
      */
     void createOrUpdate(Service service, Object newObject) throws DaoException;

     /**
      * Find a list of objects in the database.
      *
      * @param service the service to use
      * @param className the class of the object to be found
      * @param condition the condition of the object to be found, e.g. "id = '123'"
      * @param page the page number
      * @param limit the limit of the objects per page
      * @param joins if you want to join other collections
      * @return a list of objects
      * @param <T> the type of the object
      * @throws DaoException if the collection finding fails
      *
      */
     <T> List<T> findList(Service service, Class<T> className, String condition, int page, int limit,
                          String... joins) throws DaoException;

     /**
      * Find a list of objects in the database.
      *
      * @param service the service to use
      * @param className the class of the object to be found
      * @param page the page number
      * @param limit the limit of the objects per page
      * @param joins if you want to join other collections
      * @return a list of objects
      * @param <T> the type of the object
      * @throws DaoException if the collection finding fails
      *
      */
     <T> List<T> findList(Service service, Class<T> className, int page, int limit, String... joins) throws DaoException;

     /**
      * Find a list of objects in the database.
      *
      * @param service the service to use
      * @param className the class of the object to be found
      * @param joins if you want to join other collections
      * @return a list of objects
      * @param <T> the type of the object
      * @throws DaoException if the collection finding fails
      */
     <T> List<T> findList(Service service, Class<T> className, String... joins) throws DaoException;

     /**
      * Find one object in the database.
      *
      * @param service the service to use
      * @param className the class of the object to be found
      * @param conditionObject the object to be found, primary type is not compatible
      * @param condition the condition of the object to be found, e.g. "id = '123'"
      * @param joins if you want to join other collections
      * @return an object
      * @param <T> the type of the object
      * @throws DaoException if the collection finding fails
      */
     <T> T find(Service service, Class<?> className, Object conditionObject, LinkedHashMap<String, Object> conditions,
                String condition, String... joins)
             throws DaoException;

     /**
      * Find one object in the database.
      *
      * @param service the service to use
      * @param condition the condition of the object to be found, e.g. "id = '123'"
      * @param joins if you want to join other collections
      * @return an object
      * @param <T> the type of the object
      * @throws DaoException if the collection finding fails
      */
     <T> T find(Service service, Object condition, String... joins) throws DaoException;

     /**
      * Find one object in the database.
      *
      * @param service the service to use
      * @param className the class of the object to be found
      * @param condition the condition of the object to be found, e.g. "id = '123'"
      * @param joins if you want to join other collections
      * @return an object
      * @param <T> the type of the object
      * @throws DaoException if the collection finding fails
      */
     <T> T find(Service service, Class<?> className, String condition, String... joins) throws DaoException;

     /**
      * Find one object in the database by its id.
      *
      * @param service the service to use
      * @param className the class of the object to be found
      * @param id the id of the object to be found
      * @param joins if you want to join other collections
      * @return an object
      * @param <T> the type of the object
      * @throws DaoException if the collection finding fails
      */
     <T> T findById(Service service, Class<?> className, Object id, String... joins) throws DaoException;

     /**
      * Update an object in the database.
      *
      * @param service the service to use
      * @param newObject the object to be updated
      * @param conditionObject the condition of update, primary type is not compatible
      * @param condition the condition of the object to be updated, e.g. "id = '123'"
      * @throws DaoException if the collection update fails
      */
     void update(Service service, Object newObject, Object conditionObject,
                 LinkedHashMap<String, Object> conditions, String condition, int action)
             throws DaoException;

     /**
      * Update an object in the database, by its id inside the object.
      *
      * @param service the service to use
      * @param newObject the object to be updated
      * @throws DaoException if the collection update fails
      */
     void update(Service service, Object newObject, String condition) throws DaoException;

     /**
      * Update an object in the database.
      *
      * @param service the service to use
      * @param newObject the object to be updated
      * @param conditionObject the condition of update, primary type is not compatible
      * @throws DaoException if the collection update fails
      */
     void update(Service service, Object newObject, Object conditionObject) throws DaoException;


     /**
      * Update an object in the database by its id.
      *
      * @param service the service to use
      * @param newObject the object to be updated
      * @param id the id of the object to be updated
      * @throws DaoException if the collection update fails
      */
     void updateById(Service service, Object newObject, Object id) throws DaoException;

     /**
      * Delete an object in the database.
      *
      * @param service the service to use
      * @param className the class of the object to be deleted
      * @param conditionObject the condition of delete, primary type is not compatible
      * @param condition the condition of the object to be deleted, e.g. "id = '123'"
      * @throws DaoException if the collection deletion fails
      */
     void delete(Service service, Class<?> className, Object conditionObject,
                 LinkedHashMap<String, Object> conditions, String condition) throws DaoException;

     /**
      * Delete an object in the database.
      *
      * @param service the service to use
      * @param conditionObject the object to be deleted, primary type is not compatible
      * @throws DaoException if the collection deletion fails
      */
     void delete(Service service, Object conditionObject) throws DaoException;

     /**
      * Delete an object in the database.
      *
      * @param service the service to use
      * @param className the class of the object to be deleted
      * @param condition the condition of the object to be deleted, e.g. "id = '123'"
      * @throws DaoException if the collection deletion fails
      */
     void delete(Service service, Class<?> className, String condition) throws DaoException;

     /**
      * Delete an object in the database by its id.
      *
      * @param service the service to use
      * @param className the class of the object to be deleted
      * @param id the id of the object to be deleted
      * @throws DaoException if the collection deletion fails
      */
     void deleteById(Service service, Class<?> className, Object id) throws DaoException;

     /**
      * Get the next sequence value.
      *
      * @param service the service to use
      * @param sequenceName the name of the sequence
      * @return the next sequence value
      * @throws DaoException if the sequence value retrieval fails
      */
     String getNextSequenceValue(Service service, String sequenceName)
             throws DaoException;

     /**
      * Create a collection in the database.
      *
      * @param service the service to use
      * @param objectClass the class of the object to be created
      * @throws DaoException if the collection creation fails
      */
     void createCollection(Service service, Class<?> objectClass) throws DaoException,
             DatabaseException;

     /**
      * Execute a query in the database.
      *
      * @param service the service to use
      * @param query the query to be executed
      * @throws DaoException if the query execution fails
      *
      */
     void execute(Service service, String query) throws DaoException;

     /**
      * Execute a query in the database, and get one object.
      *
      * @param service the service to use
      * @param className the class of the object to be queried
      * @param query the query to be executed
      * @return an object
      * @param <T> the type of the object
      * @throws DaoException if the query execution fails
      */
     <T> T query(Service service, Class<?> className, String query) throws DaoException;

     /**
      * Execute a query in the database, and get a list of objects.
      *
      * @param service the service to use
      * @param className the class of the object to be queried
      * @param query the query to be executed
      * @param page the page number
      * @param limit the limit of the objects per page
      * @return a list of objects
      * @param <T> the type of the object
      * @throws DaoException if the query execution fails
      */
     <T> List<T> queryList(Service service, Class<T> className, String query, int page, int limit) throws DaoException;

     /**
      * Execute a query in the database, and get a list of objects.
      *
      * @param service the service to use
      * @param className the class of the object to be queried
      * @param query the query to be executed
      * @return a list of objects
      * @param <T> the type of the object
      * @throws DaoException if the query execution fails
      */
     <T> List<T> queryList(Service service, Class<T> className, String query) throws DaoException;
}
