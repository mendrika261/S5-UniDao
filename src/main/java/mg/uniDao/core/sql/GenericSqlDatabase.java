package mg.uniDao.core.sql;

import mg.uniDao.annotation.AutoSequence;
import mg.uniDao.annotation.Collection;
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
    public void loadDriver() throws DatabaseException {
        if(!DRIVER_LOADED) {
            try {
                Class.forName(DRIVER);
            } catch (ClassNotFoundException e) {
                throw new DatabaseException("Cannot find the driver - " + DRIVER);
            }
            DRIVER_LOADED = true;
        }
    }

    @Override
    public Service connect(boolean transaction) throws DatabaseException {
        loadDriver();
        final Connection connection;
        try {
            connection = DriverManager.getConnection(
                    getUrl(), getUsername(), getPassword());
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DatabaseException("Credentials are not correct");
        }
        return new Service(this, connection, transaction);
    }

    @Override
    public Service connect() throws DatabaseException {
        return connect(true);
    }

    protected abstract String getMappingType(String type);

    @Override
    public void prepareStatement(PreparedStatement preparedStatement, HashMap<Field, Object> attributes)
            throws IllegalAccessException, InvocationTargetException, SQLException {
        int i = 1;
        for (Field key : attributes.keySet()) {
            try {
                preparedStatement.setObject(i, attributes.get(key));
            } catch (Exception e) {
                preparedStatement.setString(i, Format.toJson(attributes.get(key)));
            }
            i++;
        }
    }

    @Override
    public void execute(Service service, String query, HashMap<Field, Object> parameters) throws DaoException {
        final Connection connection = (Connection) service.getAccess();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(query);
            prepareStatement(preparedStatement, parameters);

            GeneralLog.printQuery(preparedStatement.toString());
            preparedStatement.executeUpdate();
            preparedStatement.close();
            if (!service.isTransactional())
                service.endConnection();
        } catch (SQLException | IllegalAccessException | InvocationTargetException e) {
            service.endConnection();
            throw new DaoException(e.getMessage());
        }
    }

    @Override
    public void execute(Service service, String query) throws DaoException {
        execute(service, query, new HashMap<>());
    }

    protected abstract String createSQL(String collectionName, HashMap<Field, Object> attributes);

    @Override
    public void create(Service service, String collectionName, Object object) throws DaoException {
        ObjectUtils.fillAutoSequence(service, object);
        final HashMap<Field, Object> attributes = ObjectUtils.getFieldsAnnotatedNameWithValues(object);
        final String sql = createSQL(collectionName, attributes);
        execute(service, sql, attributes);
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
                    final Reference referenceAnnotation = field.getAnnotation(Reference.class);
                    final Class<?> referenceClass = referenceAnnotation.collection();
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
            final String outsideJoinCollection = ObjectUtils.getCollectionName(reference.collection());
            final String outsideJoinFieldOrCondition = reference.field();
            final List<String> columns = ObjectUtils.getColumnNamesWithChildren(reference.collection(), "");

            final Joiner joiner = new Joiner(ObjectUtils.getAnnotatedFieldName(field),
                    outsideJoinCollection,
                    outsideJoinFieldOrCondition,
                    columns);
            joiners.add(joiner);
        }
        return joiners;
    }

    @Override
    public <T> List<T> findList(Service service, String collectionName, Class<T> className, int page, int limit,
                                String extraCondition, String... joins) throws DaoException {
        final Connection connection = (Connection) service.getAccess();
        final String sql = findListWithLimitSQL(collectionName, extraCondition, extractJoinersFrom(className, joins));
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
            throw new DaoException(e.getMessage());
        }
    }

    protected abstract String findSQL(String collectionName, HashMap<Field, Object> conditions,
                                      String extraCondition, List<Joiner> joiners);

    @Override
    public <T> T find(Service service, String collectionName, Object condition, String extraCondition, String... joins)
            throws DaoException {
        final Connection connection = (Connection) service.getAccess();
        final HashMap<Field, Object> conditions = ObjectUtils.getFieldsNotNullAnnotatedNameWithValues(condition);
        final String sql = findSQL(collectionName, conditions, extraCondition,
                                        extractJoinersFrom(condition.getClass(), joins));

        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql);
            prepareStatement(preparedStatement, conditions);

            GeneralLog.printQuery(preparedStatement.toString());
            final ResultSet resultSet = preparedStatement.executeQuery();

            T object = null;
            if(resultSet.next())
                object = resultSetToObject(resultSet, (Class<T>) condition.getClass(), "", joins);

            resultSet.close();
            preparedStatement.close();

            return object;
        } catch (SQLException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                InvocationTargetException e) {
            service.endConnection();
            throw new DaoException(e.getMessage());
        }
    }

    protected abstract String updateSQL(String collectionName, HashMap<Field, Object> attributes,
                                        HashMap<Field, Object> conditions, String extraCondition);

    @Override
    public void update(Service service, String collectionName, Object condition, Object object, String extraCondition)
            throws DaoException {
        final HashMap<Field, Object> values = ObjectUtils.getFieldsNotNullAnnotatedNameWithValues(object, true);
        final HashMap<Field, Object> conditions = ObjectUtils.getFieldsNotNullAnnotatedNameWithValues(condition);
        final String sql = updateSQL(collectionName, values, conditions, extraCondition);
        values.putAll(conditions);
        execute(service, sql, values);
    }

    protected abstract String deleteSQL(String collectionName, HashMap<Field, Object> conditions, String extraCondition);

    @Override
    public void delete(Service service, String collectionName, Object condition, String extraCondition)
            throws DaoException {
        final HashMap<Field, Object> conditions = ObjectUtils.getFieldsNotNullAnnotatedNameWithValues(condition, true);
        final String sql = deleteSQL(collectionName, conditions, extraCondition);
        execute(service, sql, conditions);
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
            throw new DaoException(e.getMessage());
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
            throws DaoException, DatabaseException {
        final String sql = alterColumnTypeSQL(collectionName, columnName, columnType);
        execute(service, sql);
    }

    protected abstract String setColumnNullableSQL(String collectionName, String columnName, boolean nullable)
            throws DatabaseException;

    @Override
    public void setColumnNullable(Service service, String collectionName, String columnName, boolean nullable)
            throws DaoException, DatabaseException {
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
            throws DaoException, DatabaseException {
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
            throws DaoException, DatabaseException {
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
                             String referenceColumn) throws DaoException, DatabaseException {
        dropForeignKey(service, collectionName, columnName);
        final String sql = addForeignKeySQL(collectionName, columnName, referenceCollection, referenceColumn);
        execute(service, sql);
    }

    @Override
    public void createCollection(Service service, String collectionName, Class<?> objectClass)
            throws DaoException, DatabaseException {
        final String createSql = createCollectionSQL(collectionName);
        final Field[] fields = ObjectUtils.getDeclaredFields(objectClass);

        try {
            execute(service, createSql);

            for(Field field: fields) {
                final String addColumnSql = addColumnSQL(collectionName, ObjectUtils.getAnnotatedFieldName(field),
                        field.getType().getName());
                execute(service, addColumnSql);

                alterColumnType(service, collectionName, ObjectUtils.getAnnotatedFieldName(field),
                        field.getType().getName());

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
                    AutoSequence annotation = field.getAnnotation(AutoSequence.class);
                    createSequence(service, annotation.name() + Config.SEQUENCE_SUFFIX);
                }

                if(field.isAnnotationPresent(Reference.class)) {
                    Reference annotation = field.getAnnotation(Reference.class);
                    Collection referenceCollection = annotation.collection().getAnnotation(Collection.class);
                    if(referenceCollection == null)
                        throw new DaoException("Reference collection is not a collection (annotate with @Collection)");
                    addForeignKey(service, collectionName, ObjectUtils.getAnnotatedFieldName(field),
                            ObjectUtils.getCollectionName(annotation.collection()),
                            annotation.field());
                }
            }

            addPrimaryKey(service, collectionName, ObjectUtils.getPrimaryKeys(objectClass).values().stream().toList());

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


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
