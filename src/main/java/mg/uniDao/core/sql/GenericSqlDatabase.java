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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class GenericSqlDatabase implements GenericSqlDatabaseInterface {
    private static final String DRIVER = Config.DOTENV.get("DB_DRIVER");
    private static boolean DRIVER_LOADED = false;
    private String url = Config.DOTENV.get("DB_URL");
    private String username = Config.DOTENV.get("DB_USERNAME");
    private String password = Config.DOTENV.get("DB_PASSWORD");
    private static final String COL_NAME_SEPARATOR = ".";

    public GenericSqlDatabase() {
    }


    @Override
    public void loadDriver() throws DaoException {
        if(!DRIVER_LOADED) {
            if(DRIVER == null)
                throw new DaoException("Driver is not set in the .env file");
            try {
                Class.forName(DRIVER);
            } catch (ClassNotFoundException e) {
                throw new DaoException("Cannot find the driver: " + DRIVER);
            }
            DRIVER_LOADED = true;
        }
    }

    @Override
    public Service connect(boolean transaction) throws DaoException {
        loadDriver();
        final Connection connection;
        try {
            connection = DriverManager.getConnection(getUrl(), getUsername(), getPassword());
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DaoException("Credentials are not correct");
        }

        return new Service(this, connection, transaction);
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
        throw new DaoException("No mappings");
    }

    @Override
    public void prepareStatement(PreparedStatement preparedStatement, HashMap<Field, Object> attributes) {
        int i = 1;
        for (Field key : attributes.keySet()) {
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
    public void execute(Service service, String query, HashMap<Field, Object> parameters) throws DaoException {
        final Connection connection = (Connection) service.getAccess();
        final PreparedStatement preparedStatement;
        final long startTime = System.currentTimeMillis();
        int resultSize = 0;
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
        execute(service, query, new HashMap<>());
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


    protected abstract String createSQL(String collectionName, HashMap<Field, Object> attributes);

    @Override
    public void create(Service service, Object object) throws DaoException {
        ObjectUtils.fillAutoSequence(service, object);
        final HashMap<Field, Object> attributes = ObjectUtils.getFieldsAnnotatedNameWithValues(object);
        final String sql = createSQL(ObjectUtils.getCollectionName(object.getClass()), attributes);
        execute(service, sql, attributes);
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
    public boolean existsById(Service service, Class<?> className, String id) throws DaoException {
        HashMap<Field, Object> conditions = new HashMap<>();
        conditions.put(ObjectUtils.getDeclaredField(className,
                ObjectUtils.getPrimaryKeys(className).values().stream().toList().get(0)), id);
        return find(service, className, conditions, "") != null;
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

    protected abstract String findListWithLimitSQL(String collectionName, String extraCondition,
                                                   List<Joiner> joiners);

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

    protected abstract String findSQL(String collectionName, HashMap<Field, Object> conditions,
                                      String extraCondition, List<Joiner> joiners);

    @Override
    public <T> T find(Service service, Class<?> className, Object conditionObject, String condition, String... joins)
            throws DaoException {
        final Connection connection = (Connection) service.getAccess();
        final HashMap<Field, Object> conditions = ObjectUtils.getFieldsNotNullAnnotatedNameWithValues(conditionObject);
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
        return find(service, conditionObject.getClass(), conditionObject, "", joins);
    }

    @Override
    public <T> T find(Service service, Class<?> className, String condition, String... joins)
            throws DaoException {
        return find(service, className, null, condition, joins);
    }

    @Override
    public <T> T findById(Service service, Class<?> className, String id, String... joins) throws DaoException {
        final HashMap<Field, Object> conditions = new HashMap<>();
        conditions.put(ObjectUtils.getDeclaredField(className,
                ObjectUtils.getPrimaryKeys(className).values().stream().toList().get(0)), id);
        return find(service, className, conditions, "", joins);
    }

    protected abstract String updateSQL(String collectionName, HashMap<Field, Object> attributes,
                                        HashMap<Field, Object> conditions, String extraCondition);

    @Override
    public void update(Service service, Object newObject, Object conditionObject, String condition)
            throws DaoException {
        final HashMap<Field, Object> values = ObjectUtils.getFieldsNotNullAnnotatedNameWithValues(newObject, true);
        final HashMap<Field, Object> conditions = ObjectUtils.getFieldsNotNullAnnotatedNameWithValues(conditionObject);
        final String sql = updateSQL(ObjectUtils.getCollectionName(newObject.getClass()), values, conditions, condition);
        values.putAll(conditions);
        execute(service, sql, values);
    }

    @Override
    public void update(Service service, Object newObject, String condition) throws DaoException {
        update(service, newObject, null, condition);
    }

    @Override
    public void update(Service service, Object newObject, Object conditionObject) throws DaoException {
        update(service, conditionObject, newObject, "");
    }

    @Override
    public void updateById(Service service, Object object, String id) throws DaoException {
        final HashMap<Field, Object> conditions = new HashMap<>();
        conditions.put(ObjectUtils.getDeclaredField(object.getClass(),
                ObjectUtils.getPrimaryKeys(object.getClass()).values().stream().toList().get(0)), id);
        update(service, object, conditions, "");
    }


    protected abstract String deleteSQL(String collectionName, HashMap<Field, Object> conditions, String extraCondition);

    @Override
    public void delete(Service service, Class<?> className, Object conditionObject, String condition)
            throws DaoException {
        final HashMap<Field, Object> conditions = ObjectUtils.getFieldsNotNullAnnotatedNameWithValues(conditionObject, true);
        final String sql = deleteSQL(ObjectUtils.getCollectionName(className), conditions, condition);
        execute(service, sql, conditions);
    }

    @Override
    public void delete(Service service, Object conditionObject) throws DaoException {
        if (conditionObject == null)
            throw new DatabaseException("Condition cannot be null");
        delete(service, conditionObject.getClass(), conditionObject, "");
    }

    @Override
    public void delete(Service service, Class<?> className, String condition) throws DaoException {
        delete(service, className, null, condition);
    }

    @Override
    public void deleteById(Service service, Class<?> className, String id) throws DaoException {
        final HashMap<Field, Object> conditions = new HashMap<>();
        conditions.put(ObjectUtils.getDeclaredField(className, ObjectUtils.getPrimaryKeys(className).values().stream().toList().get(0)), id);
        delete(service, className, conditions, "");
    }

    protected abstract String getNextSequenceValueSql(String sequenceName);

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

    protected abstract String dropCollectionSQL(String collectionName);

    @Override
    public void dropCollection(Service service, String collectionName) throws DaoException {
        final String sql = dropCollectionSQL(collectionName);
        execute(service, sql);
    }

    protected abstract String dropPrimaryKeySQL(String collectionName);

    @Override
    public void dropPrimaryKey(Service service, String collectionName) throws DaoException {
        final String sql = dropPrimaryKeySQL(collectionName);
        execute(service, sql);
    }

    protected abstract String addPrimaryKeySQL(String collectionName, List<String> primaryKeyColumns);

    @Override
    public void addPrimaryKey(Service service, String collectionName, List<String> primaryKeyColumns)
            throws DaoException {
        dropPrimaryKey(service, collectionName);
        final String sql = addPrimaryKeySQL(collectionName, primaryKeyColumns);
        execute(service, sql);
    }

    protected abstract String alterColumnTypeSQL(String collectionName, String columnName, String columnType)
            throws DatabaseException;

    @Override
    public void alterColumnType(Service service, String collectionName, String columnName, String columnType)
            throws DaoException {
        final String sql = alterColumnTypeSQL(collectionName, columnName, columnType);
        execute(service, sql);
    }

    protected abstract String setColumnNullableSQL(String collectionName, String columnName, boolean nullable);

    @Override
    public void setColumnNullable(Service service, String collectionName, String columnName, boolean nullable)
            throws DaoException {
        final String sql = setColumnNullableSQL(collectionName, columnName, nullable);
        execute(service, sql);
    }

    protected abstract String createSequenceSQL(String sequenceName);

    @Override
    public void createSequence(Service service, String sequenceName) throws DaoException {
        final String sql = createSequenceSQL(sequenceName);
        execute(service, sql);
    }

    protected abstract String addColumnUniqueSQL(String collectionName, String columnName);

    protected abstract String dropColumnUniqueSQL(String collectionName, String columnName);

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

    protected abstract String addUniqueSQL(String collectionName, String[] columnName);

    protected abstract String dropUniqueSQL(String collectionName);

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

    protected abstract String createCollectionSQL(String collectionName);

    protected abstract String addColumnSQL(String collectionName, String columnName, String columnType)
            throws DatabaseException;

    protected abstract String addForeignKeySQL(String collectionName, String columnName, String referenceCollection,
                                              String referenceColumn) throws DatabaseException;

    protected abstract String dropForeignKeySQL(String collectionName, String columnName);

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
                final String mappingType =  getMappingType(field);
                final String addColumnSql = addColumnSQL(collectionName, ObjectUtils.getAnnotatedFieldName(field),
                       mappingType);
                execute(service, addColumnSql);

                alterColumnType(service, collectionName, ObjectUtils.getAnnotatedFieldName(field), mappingType);

                if(field.isAnnotationPresent(mg.uniDao.annotation.Field.class)) {
                    mg.uniDao.annotation.Field annotation = field.getAnnotation(mg.uniDao.annotation.Field.class);
                    if(!annotation.isPrimaryKey()) {
                        setColumnNullable(service, collectionName, ObjectUtils.getAnnotatedFieldName(field),
                                annotation.isNullable());

                        setColumnUnique(service, collectionName, ObjectUtils.getAnnotatedFieldName(field),
                                annotation.isUnique());
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
                    addForeignKey(service, collectionName, ObjectUtils.getAnnotatedFieldName(field),
                            ObjectUtils.getCollectionName(field.getType()),
                            annotation.field());
                }
            }

            if(!objectClass.isAnnotationPresent(Link.class))
                addPrimaryKey(service, collectionName,
                        ObjectUtils.getPrimaryKeys(objectClass).values().stream().toList());

            if(objectClass.isAnnotationPresent(Collection.class)) {
                Collection annotation = objectClass.getAnnotation(Collection.class);
                setUnique(service, collectionName, annotation.uniqueFields());
            }
        } catch (DaoException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DaoException(e.getMessage());
        }
    }

    public String getUrl() throws DaoException {
        if (url == null)
            throw new DaoException("Database url is not set in the .env file");
        return url;
    }

    public void setUrl(String url) throws DaoException {
        this.url = url;
    }

    public String getUsername() throws DaoException {
        if (username == null)
            throw new DaoException("Database username is not set in the .env file");
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        if (password == null || password.isEmpty())
            GeneralLog.printWarning("You are using the database without a password");
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
