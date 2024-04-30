package mg.uniDao.core.sql;

import mg.uniDao.annotation.AutoSequence;
import mg.uniDao.annotation.Collection;
import mg.uniDao.annotation.Link;
import mg.uniDao.annotation.Reference;
import mg.uniDao.core.Service;
import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;
import mg.uniDao.log.GeneralLog;
import mg.uniDao.util.Config;
import mg.uniDao.util.Format;
import mg.uniDao.util.ObjectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class GenericSqlDatabase implements GenericSqlDatabaseInterface {
    private final String COL_NAME_SEPARATOR = ".";
    public final int ACTION_CREATE = 1;
    public final int ACTION_UPDATE = 2;
    public final int ACTION_DELETE = 3;
    public final String ACTION_LAST_REFERENCE_COLUMN_NAME = "unidao_reference";
    public final String ACTION_DATE_COLUMN_NAME = "unidao_action_date";
    public final String ACTION_DELETE_DATE_COLUMN_NAME = "unidao_delete_date";

    private boolean DRIVER_LOADED = false;

    public GenericSqlDatabase() {}


    @Override
    public void loadDriver(String configName) throws DaoException {
        if(!DRIVER_LOADED) {
            if(getDriver(configName) == null)
                throw new DaoException("Driver for " + configName + " must be set in the .env file");
            try {
                Class.forName(getDriver(configName));
                GeneralLog.printInfo("Loaded driver: " + getDriver(configName));
            } catch (ClassNotFoundException e) {
                throw new DaoException("Cannot find the driver: " + getDriver(configName));
            }
            DRIVER_LOADED = true;
        }
    }

    @Override
    public Service connect(String configName, boolean transaction) throws DaoException {
        loadDriver(configName);
        final Connection connection;
        try {
            connection = DriverManager.getConnection(getUrl(configName), getUsername(configName),
                    getPassword(configName));
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DaoException("Credentials are not correct");
        }

        return new Service(this, connection, transaction);
    }

    @Override
    public Service connect(String configName) throws DaoException {
        return connect(configName, true);
    }

    @Override
    public Service connect(boolean transaction) throws DaoException {
        String defaultDb = Config.DOTENV.get("DB_DEFAULT", "");
        return connect(defaultDb, transaction);
    }

    @Override
    public Service connect() throws DaoException {
        return connect(true);
    }

    protected String getMappingType(Field field) throws DaoException {
        if(field.isAnnotationPresent(mg.uniDao.annotation.Field.class)) {
            final String columnTypeImposed = field.getAnnotation(mg.uniDao.annotation.Field.class).databaseMappingType();
            if(!columnTypeImposed.isEmpty())
                return columnTypeImposed;
        }
        return getMappingType(field.getType());
    }

    public String getMappingType(Class<?> className) throws DaoException {
        throw new DaoException("No mappings");
    }

    @Override
    public void prepareStatement(PreparedStatement preparedStatement, LinkedHashMap<String, Object> attributes) {
        int i = 1;
        for (String key : attributes.keySet()) {
            try {
                preparedStatement.setObject(i, attributes.get(key));
            } catch (Exception ignored) {
                try {
                    preparedStatement.setString(i, Format.toJson(attributes.get(key)));
                } catch (Exception ignored2) {
                    throw new DatabaseException("Cannot set as string: \n" + Format.toJson(attributes.get(key)));
                }
            }
            i++;
        }
    }

    @Override
    public void execute(Service service, String query, LinkedHashMap<String, Object> parameters) throws DaoException {
        final Connection connection = (Connection) service.getAccess();
        final PreparedStatement preparedStatement;
        final long startTime = System.currentTimeMillis();
        int resultSize;
        try {
            preparedStatement = connection.prepareStatement(query);
            prepareStatement(preparedStatement, parameters);

            GeneralLog.printQuery(preparedStatement.toString());
            resultSize = preparedStatement.executeUpdate();
            preparedStatement.close();

            final long duration = System.currentTimeMillis() - startTime;
            GeneralLog.printInfo("Executed in " + duration + "ms, changing " + resultSize + " row(s)");
            if (!service.isTransactional())
                service.endConnection();
        } catch (SQLException e) {
            service.endConnection();
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public void execute(Service service, String query) throws DaoException {
        execute(service, query, new LinkedHashMap<>());
    }

    @Override
    public <T> T query(Service service, Class<?> className, String query) throws DaoException {
        final Connection connection = (Connection) service.getAccess();

        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(query);

            GeneralLog.printQuery(preparedStatement.toString());
            final ResultSet resultSet = preparedStatement.executeQuery();

            T object = null;
            if(resultSet.next())
                object = resultSetToObject(resultSet, (Class<T>) className, "");

            resultSet.close();
            preparedStatement.close();

            return object;
        } catch (SQLException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            service.endConnection();
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public <T> List<T> queryList(Service service, Class<T> className, String query, int page, int limit) throws DaoException {
        final Connection connection = (Connection) service.getAccess();
        final List<T> objects = new ArrayList<>();

        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, limit);
            preparedStatement.setInt(2, (page - 1) * limit);

            GeneralLog.printQuery(preparedStatement.toString());
            final ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                final T object = resultSetToObject(resultSet, className, "");
                objects.add(object);
            }

            resultSet.close();
            preparedStatement.close();

            return objects;
        } catch (SQLException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            service.endConnection();
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public <T> List<T> queryList(Service service, Class<T> className, String query) throws DaoException {
        final Connection connection = (Connection) service.getAccess();
        final List<T> objects = new ArrayList<>();

        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(query);

            GeneralLog.printQuery(preparedStatement.toString());
            final ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                final T object = resultSetToObject(resultSet, className, "");
                objects.add(object);
            }

            resultSet.close();
            preparedStatement.close();

            return objects;
        } catch (SQLException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            service.endConnection();
            throw new DatabaseException(e.getMessage());
        }
    }

    public void insert(Service service, Object object, int action) throws DaoException {
        Object actualId = ObjectUtils.getId(object);
        ObjectUtils.fillAutoSequence(service, object);
        final LinkedHashMap<String, Object> attributes = ObjectUtils.getFieldsAnnotatedNameWithValues(object);

        if(ObjectUtils.isToHistorize(object.getClass())) {
            if(action == ACTION_UPDATE) {
                attributes.put(ACTION_LAST_REFERENCE_COLUMN_NAME, actualId);
            } else {
                attributes.put(ACTION_LAST_REFERENCE_COLUMN_NAME, ObjectUtils.getId(object));
            }
            attributes.put(ACTION_DATE_COLUMN_NAME, LocalDateTime.now());
        }

        final String sql = createSQL(ObjectUtils.getCollectionName(object.getClass()), attributes);
        execute(service, sql, attributes);
    }

    @Override
    public void create(Service service, Object object) throws DaoException {
        insert(service, object, ACTION_CREATE);
    }

    @Override
    public boolean exists(Service service, Class<?> className, String condition) throws DaoException {
        return find(service, className, condition) != null;
    }

    @Override
    public boolean exists(Service service, Object conditionObject) throws DaoException {
        return find(service, conditionObject) != null;
    }

    @Override
    public boolean existsById(Service service, Class<?> className, Object id) throws DaoException {
        return findById(service, className, id) != null;
    }

    @Override
    public void createOrUpdate(Service service, Object newObject) throws DaoException {
        if(exists(service, newObject))
            updateById(service, newObject, ObjectUtils.getId(newObject));
        else
            create(service, newObject);
    }

    private <T> T resultSetToObject(ResultSet resultSet, Class<T> className, String reference, String... join)
            throws SQLException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException, DaoException {
        final Field[] fields = ObjectUtils.getDeclaredFields(className);
        final T object = className.getDeclaredConstructor().newInstance();
        reference = reference.isEmpty() ? "": (reference + COL_NAME_SEPARATOR);
        for (Field field : fields) {
            if(!field.isAnnotationPresent(Reference.class)) {
                ObjectUtils.setFieldValue(
                        object,
                        field,
                        resultSet.getObject(reference+ObjectUtils.getAnnotatedFieldName(field))
                );
            } else {
                String finalReference = reference;
                if(join != null && join.length > 0 &&
                        Arrays.stream(join).anyMatch(joiner ->
                                joiner.equalsIgnoreCase(finalReference + field.getName()))) {
                    final Class<?> referenceClass = field.getType();
                    final String referencePrefix = reference + ObjectUtils.getCollectionName(referenceClass);
                    final Object referenceObject = resultSetToObject(resultSet, referenceClass, referencePrefix);
                    ObjectUtils.setFieldValue(object, field, referenceObject);
                } else { // set only the reference id
                    final String columnName = ObjectUtils.getAnnotatedFieldName(field);
                    final Object referencedObject = field.getType().getDeclaredConstructor().newInstance();
                    final String primaryKey = ObjectUtils.getPrimaryKeys(field.getType()).values().stream().toList().get(0);
                    ObjectUtils.setFieldValue(
                            referencedObject,
                            ObjectUtils.getDeclaredField(field.getType(), primaryKey),
                            resultSet.getObject(reference+columnName)
                    );
                    ObjectUtils.setFieldValue(
                            object,
                            field,
                            referencedObject
                    );
                }
            }
        }
        return object;
    }



    private List<Joiner> extractJoinersFrom(Class<?> className, String... joins) throws DaoException {
        List<Joiner> joiners = new ArrayList<>();
        for (String join: joins) {
            final String[] joinParts = join.split("\\"+COL_NAME_SEPARATOR);
            final String insideJoinField = joinParts[joinParts.length - 1];
            final Field field = ObjectUtils.getDeclaredField(className, insideJoinField);
            final Reference reference = field.getAnnotation(Reference.class);
            final String outsideJoinCollection = ObjectUtils.getCollectionName(field.getType());
            final String outsideJoinFieldOrCondition = reference.field();
            final List<String> columns = ObjectUtils.getColumnNamesWithChildren(field.getType(), "");

            final Joiner joiner = new Joiner(ObjectUtils.getAnnotatedFieldName(field),
                    outsideJoinCollection,
                    outsideJoinFieldOrCondition,
                    columns);
            joiners.add(joiner);
        }
        return joiners;
    }

    @Override
    public <T> List<T> findList(Service service, Class<T> className, String condition, int page, int limit,
                                String... joins) throws DaoException {
        final Connection connection = (Connection) service.getAccess();
        final String sql = findListWithLimitSQL(ObjectUtils.getCollectionName(className), condition,
                extractJoinersFrom(className, joins));
        final List<T> objects = new ArrayList<T>();

        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, limit);
            preparedStatement.setInt(2, (page - 1) * limit);

            GeneralLog.printQuery(preparedStatement.toString());
            final ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                final T object = resultSetToObject(resultSet, className, "", joins);
                objects.add(object);
            }

            resultSet.close();
            preparedStatement.close();

            return objects;
        } catch (SQLException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            service.endConnection();
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public <T> List<T> findList(Service service, Class<T> className, int page, int limit, String... joins)
            throws DaoException {
        return findList(service, className, "", page, limit, joins);
    }

    @Override
    public <T> List<T> findList(Service service, Class<T> className, String... joins) throws DaoException {
        GeneralLog.printWarning("You are fetching the whole list of " + className.getName() + " from the database");
        return findList(service, className, "", 1, Integer.MAX_VALUE, joins);
    }

    @Override
    public <T> T find(Service service, Class<?> className, Object conditionObject,
                      LinkedHashMap<String, Object> conditions,
                      String condition, String... joins)
            throws DaoException {
        final Connection connection = (Connection) service.getAccess();

        if(conditionObject != null)
            conditions.putAll(ObjectUtils.getFieldsNotNullAnnotatedNameWithValues(conditionObject));
        final String sql = findSQL(ObjectUtils.getCollectionName(className), conditions, condition,
                                        extractJoinersFrom(className, joins));

        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql);
            prepareStatement(preparedStatement, conditions);

            GeneralLog.printQuery(preparedStatement.toString());
            final ResultSet resultSet = preparedStatement.executeQuery();

            T object = null;
            if(resultSet.next())
                object = resultSetToObject(resultSet, (Class<T>) className, "", joins);

            resultSet.close();
            preparedStatement.close();

            return object;
        } catch (SQLException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                InvocationTargetException e) {
            service.endConnection();
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public <T> T find(Service service, Object conditionObject, String... joins) throws DaoException {
        if (conditionObject == null)
            throw new DatabaseException("Condition cannot be null");
        return find(service, conditionObject.getClass(), conditionObject, new LinkedHashMap<>(), "", joins);
    }

    @Override
    public <T> T find(Service service, Class<?> className, String condition, String... joins)
            throws DaoException {
        return find(service, className, null, new LinkedHashMap<>(), condition, joins);
    }

    @Override
    public <T> T findById(Service service, Class<?> className, Object id, String... joins) throws DaoException {
        final LinkedHashMap<String, Object> conditions = new LinkedHashMap<>();
        conditions.put(ObjectUtils.getPrimaryKeys(className).values().stream().toList().get(0), id);
        return find(service, className, null, conditions, "", joins);
    }

    @Override
    public void update(Service service, Object newObject, Object conditionObject,
                       LinkedHashMap<String, Object> conditions, String condition, int action)
            throws DaoException {
        final LinkedHashMap<String, Object> values = ObjectUtils
                .getFieldsNotNullAnnotatedNameWithValuesWithoutPk(newObject, true);

        if(ObjectUtils.isToHistorize(newObject.getClass())) {
            if(action == ACTION_UPDATE) {
                insert(service, newObject, ACTION_UPDATE);
                return;
            } else if(action == ACTION_DELETE) {
                values.put(ACTION_DELETE_DATE_COLUMN_NAME, LocalDateTime.now());
            }
        }

        if(conditionObject != null)
            conditions.putAll(ObjectUtils.getFieldsNotNullAnnotatedNameWithValues(conditionObject));

        final String sql = updateSQL(ObjectUtils.getCollectionName(newObject.getClass()), values, conditions, condition);
        values.putAll(conditions);
        execute(service, sql, values);
    }

    @Override
    public void update(Service service, Object newObject, String condition) throws DaoException {
        update(service, newObject, null, new LinkedHashMap<>(), condition, ACTION_UPDATE);
    }

    @Override
    public void update(Service service, Object newObject, Object conditionObject) throws DaoException {
        update(service, conditionObject, newObject, new LinkedHashMap<>(), "", ACTION_UPDATE);
    }

    @Override
    public void updateById(Service service, Object object, Object id) throws DaoException {
        final LinkedHashMap<String, Object> conditions = new LinkedHashMap<>();
        conditions.put(ObjectUtils.getPrimaryKeys(object.getClass()).values().stream().toList().get(0), id);
        update(service, object, null, conditions, "", ACTION_UPDATE);
    }

    @Override
    public void delete(Service service, Class<?> className, Object conditionObject,
                       LinkedHashMap<String, Object> conditions, String condition) throws DaoException {
        if(ObjectUtils.isToHistorize(className)) {
            Object instance = ObjectUtils.newInstance(className);
            update(service, instance, conditionObject, conditions, condition, ACTION_DELETE);
            return;
        }

        if(conditionObject != null)
            conditions.putAll(ObjectUtils.getFieldsNotNullAnnotatedNameWithValues(conditionObject, true));
        final String sql = deleteSQL(ObjectUtils.getCollectionName(className), conditions, condition);
        execute(service, sql, conditions);
    }

    @Override
    public void delete(Service service, Object conditionObject) throws DaoException {
        if (conditionObject == null)
            throw new DatabaseException("Condition cannot be null");
        delete(service, conditionObject.getClass(), conditionObject, new LinkedHashMap<>(), "");
    }

    @Override
    public void delete(Service service, Class<?> className, String condition) throws DaoException {
        delete(service, className, null, new LinkedHashMap<>(), condition);
    }

    @Override
    public void deleteById(Service service, Class<?> className, Object id) throws DaoException {
        final LinkedHashMap<String, Object> conditions = new LinkedHashMap<>();
        conditions.put(ObjectUtils.getPrimaryKeys(className).values().stream().toList().get(0), id);
        delete(service, className, null, conditions, "");
    }

    @Override
    public String getNextSequenceValue(Service service, String sequenceName) throws DaoException {
        String sql = getNextSequenceValueSql(sequenceName);

        try {
            final PreparedStatement preparedStatement = ((Connection) service.getAccess()).prepareStatement(sql);

            GeneralLog.printQuery(preparedStatement.toString());
            final ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();
            final String result = resultSet.getString(1);

            resultSet.close();
            preparedStatement.close();
            return result;
        } catch (SQLException e) {
            service.endConnection();
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public void dropCollection(Service service, String collectionName) throws DaoException {
        final String sql = dropCollectionSQL(collectionName);
        execute(service, sql);
    }

    @Override
    public void dropPrimaryKey(Service service, String collectionName) throws DaoException {
        final String sql = dropPrimaryKeySQL(collectionName);
        execute(service, sql);
    }

    @Override
    public void addPrimaryKey(Service service, String collectionName, List<String> primaryKeyColumns)
            throws DaoException {
        dropPrimaryKey(service, collectionName);
        final String sql = addPrimaryKeySQL(collectionName, primaryKeyColumns);
        execute(service, sql);
    }

    @Override
    public void alterColumnType(Service service, String collectionName, String columnName, String columnType)
            throws DaoException {
        final String sql = alterColumnTypeSQL(collectionName, columnName, columnType);
        execute(service, sql);
    }

    @Override
    public void setColumnNullable(Service service, String collectionName, String columnName, boolean nullable)
            throws DaoException {
        final String sql = setColumnNullableSQL(collectionName, columnName, nullable);
        execute(service, sql);
    }

    @Override
    public void createSequence(Service service, String sequenceName) throws DaoException {
        final String sql = createSequenceSQL(sequenceName);
        execute(service, sql);
    }

    @Override
    public void dropColumnUnique(Service service, String collectionName, String columnName) throws DaoException {
        final String sql = dropColumnUniqueSQL(collectionName, columnName);
        execute(service, sql);
    }

    @Override
    public void setColumnUnique(Service service, String collectionName, String columnName, boolean unique)
            throws DaoException {
        dropColumnUnique(service, collectionName, columnName);
        if(unique) {
            final String addSQL = addColumnUniqueSQL(collectionName, columnName);
            execute(service, addSQL);
        }
    }

    @Override
    public void setUnique(Service service, String collectionName, String[] columnName)
            throws DaoException {
        final String dropSQL = dropUniqueSQL(collectionName);
        execute(service, dropSQL);
        if(columnName.length > 0) {
            final String addSQL = addUniqueSQL(collectionName, columnName);
            execute(service, addSQL);
        }
    }

    protected void addColumn(Service service, String collectionName, String fieldName, String mappingType)
            throws DatabaseException, DaoException {
        final String addColumnSql = addColumnSQL(collectionName, fieldName, mappingType);
        execute(service, addColumnSql);
        alterColumnType(service, collectionName, fieldName, mappingType);
    }

    @Override
    public void dropForeignKey(Service service, String collectionName, String columnName) throws DaoException {
        final String sql = dropForeignKeySQL(collectionName, columnName);
        execute(service, sql);
    }

    @Override
    public void addForeignKey(Service service, String collectionName, String columnName, String referenceCollection,
                             String referenceColumn) throws DaoException {
        dropForeignKey(service, collectionName, columnName);
        final String sql = addForeignKeySQL(collectionName, columnName, referenceCollection, referenceColumn);
        execute(service, sql);
    }

    @Override
    public void createCollection(Service service, Class<?> objectClass)
            throws DaoException {
        final String collectionName = ObjectUtils.getCollectionName(objectClass);
        final String createSql = createCollectionSQL(collectionName);
        final Field[] fields = ObjectUtils.getDeclaredFields(objectClass);

        try {
            execute(service, createSql);

            for(Field field: fields) {
                final String mappingType = getMappingType(field);
                final String fieldName = ObjectUtils.getAnnotatedFieldName(field);
                addColumn(service, collectionName, fieldName, mappingType);

                if(field.isAnnotationPresent(mg.uniDao.annotation.Field.class)) {
                    mg.uniDao.annotation.Field annotation = field.getAnnotation(mg.uniDao.annotation.Field.class);
                    if(!annotation.isPrimaryKey()) {
                        setColumnNullable(service, collectionName, fieldName, annotation.isNullable());
                        if(!ObjectUtils.isToHistorize(objectClass))
                            setColumnUnique(service, collectionName, fieldName, annotation.isUnique());
                    }
                }

                if(field.isAnnotationPresent(AutoSequence.class)) {
                    if(field.getType() != String.class)
                        throw new DaoException("Auto sequence field: '" + field.getName() + "' must be a String " +
                                "in " + objectClass.getName());
                    AutoSequence annotation = field.getAnnotation(AutoSequence.class);
                    createSequence(service, annotation.name() + Config.SEQUENCE_SUFFIX);
                }

                if(field.isAnnotationPresent(Reference.class)) {
                    Reference annotation = field.getAnnotation(Reference.class);
                    Collection referenceCollection = field.getType().getAnnotation(Collection.class);
                    if(referenceCollection == null)
                        throw new DaoException("Referenced type " + field.getType() +
                                " is not a collection (annotate with @Collection)");
                    addForeignKey(service, collectionName, fieldName,
                            ObjectUtils.getCollectionName(field.getType()),
                            annotation.field());
                }
            }

            if(!objectClass.isAnnotationPresent(Link.class))
                addPrimaryKey(service, collectionName,
                        ObjectUtils.getPrimaryKeys(objectClass).values().stream().toList());

            if(objectClass.isAnnotationPresent(Collection.class)) {
                final Collection annotation = objectClass.getAnnotation(Collection.class);
                setUnique(service, collectionName, annotation.uniqueFields());
                String idColumn = ObjectUtils.getPrimaryKeys(objectClass).values().stream().toList().get(0);
                if(annotation.historize()) {
                    addColumn(service, collectionName, ACTION_LAST_REFERENCE_COLUMN_NAME, getMappingType(String.class));
                    //addForeignKey(service, collectionName, ACTION_LAST_REFERENCE_COLUMN_NAME,
                    //        collectionName, idColumn);
                    addColumn(service, collectionName, ACTION_DATE_COLUMN_NAME, getMappingType(LocalDateTime.class));
                    addColumn(service, collectionName, ACTION_DELETE_DATE_COLUMN_NAME,
                            getMappingType(LocalDateTime.class));
                }
            }
        } catch (DaoException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DaoException(e.getMessage());
        }
    }

    public String getUrl(String configName) {
        return Config.DOTENV.get("DB_" + configName + "_URL");
    }


    public String getUsername(String configName) {
        return Config.DOTENV.get("DB_" + configName + "_USERNAME");
    }

    public String getPassword(String configName) {
        return Config.DOTENV.get("DB_" + configName + "_PASSWORD");
    }

    public String getDriver(String configName) {
        return Config.DOTENV.get("DB_" + configName + "_DRIVER");
    }
}
