package mg.uniDao.core;

import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class GenericSqlDatabase implements Database {
    private static final String DRIVER = Config.DOTENV.get("DB_DRIVER");
    private static boolean DRIVER_LOADED = false;
    private String url = Config.DOTENV.get("DB_URL");
    private String username = Config.DOTENV.get("DB_USERNAME");
    private String password = Config.DOTENV.get("DB_PASSWORD");

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

    private void prepareStatement(PreparedStatement preparedStatement, HashMap<String, Object> attributes, int start)
            throws IllegalAccessException, InvocationTargetException, DaoException, SQLException {
        int i = start;
        for (String key : attributes.keySet()) {
            //if (attributes.get(key) == null) {
            //    preparedStatement.setNull(i, Types.NULL);
            preparedStatement.setObject(i, attributes.get(key));
            i++;
        }
    }

    public void execute(Service service, String query, HashMap<String, Object> parameters) throws DaoException {
        final Connection connection = (Connection) service.getAccess();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(query);
            prepareStatement(preparedStatement, parameters, 1);

            preparedStatement.executeUpdate();
            preparedStatement.close();
            if(!service.isTransactional())
                service.endConnection();
        } catch (SQLException | IllegalAccessException | InvocationTargetException e) {
            throw new DaoException(e.getMessage());
        }
    }

    public void execute(Service service, String query) throws DaoException {
        execute(service, query, new HashMap<>());
    }

    protected abstract String createSQL(String collectionName, HashMap<String, Object> attributes);

    @Override
    public void create(Service service, String collectionName, Object object) throws DaoException {
        Utils.fillAutoSequence(service, object);
        final HashMap<String, Object> attributes = Utils.getFieldsAnnotatedNameWithValues(object);
        final String sql = createSQL(collectionName, attributes);
        execute(service, sql, attributes);
    }

    private <T> T resultSetToObject(ResultSet resultSet, Class<T> className) throws SQLException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException, DaoException {
        final Field[] fields = Utils.getDeclaredFields(className);
        final T object = className.getDeclaredConstructor().newInstance();
        for (Field field : fields)
            Utils.setFieldValue(object, field, resultSet.getObject(Utils.getAnnotatedFieldName(field)));
        return object;
    }

    protected abstract String findListWithLimitSQL(String collectionName, String extraCondition);

    @Override
    public <T> List<T> findList(Service service, String collectionName, Class<T> className, int page, int limit,
                                String extraCondition) throws DaoException {
        final Connection connection = (Connection) service.getAccess();
        final String sql = findListWithLimitSQL(collectionName, extraCondition);
        final List<T> objects = new ArrayList<T>();

        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, limit);
            preparedStatement.setInt(2, (page - 1) * limit);
            final ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                final T object = resultSetToObject(resultSet, className);
                objects.add(object);
            }

            resultSet.close();
            preparedStatement.close();

            return objects;
        } catch (SQLException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new DaoException(e.getMessage());
        }
    }

    protected abstract String findSQL(String collectionName, HashMap<String, Object> conditions, String extraCondition);

    @Override
    public <T> T find(Service service, String collectionName, Object condition, String extraCondition)
            throws DaoException {
        final Connection connection = (Connection) service.getAccess();
        final HashMap<String, Object> conditions = Utils.getFieldsNotNullAnnotatedNameWithValues(condition);
        final String sql = findSQL(collectionName, conditions, extraCondition);

        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql);
            prepareStatement(preparedStatement, conditions, 1);
            final ResultSet resultSet = preparedStatement.executeQuery();

            T object = null;
            if(resultSet.next())
                object = resultSetToObject(resultSet, (Class<T>) condition.getClass());

            resultSet.close();
            preparedStatement.close();

            return object;
        } catch (SQLException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                InvocationTargetException e) {
            throw new DaoException(e.getMessage());
        }
    }

    protected abstract String updateSQL(String collectionName, HashMap<String, Object> attributes,
                                        HashMap<String, Object> conditions, String extraCondition);

    @Override
    public void update(Service service, String collectionName, Object condition, Object object, String extraCondition) throws DaoException {
        final HashMap<String, Object> values = Utils.getFieldsNotNullAnnotatedNameWithValues(object, true);
        final HashMap<String, Object> conditions = Utils.getFieldsNotNullAnnotatedNameWithValues(condition);
        final String sql = updateSQL(collectionName, values, conditions, extraCondition);
        values.putAll(conditions);
        execute(service, sql, values);
    }

    protected abstract String deleteSQL(String collectionName, HashMap<String, Object> conditions, String extraCondition);

    @Override
    public void delete(Service service, String collectionName, Object condition, String extraCondition) throws DaoException {
        final HashMap<String, Object> conditions = Utils.getFieldsNotNullAnnotatedNameWithValues(condition, true);
        final String sql = deleteSQL(collectionName, conditions, extraCondition);
        System.out.println(sql);
        execute(service, sql, conditions);
    }

    protected abstract String getNextSequenceValueSql(String sequenceName);

    public String getNextSequenceValue(Service service, String sequenceName) throws DaoException {
        String sql = getNextSequenceValueSql(sequenceName);

        try {
            final PreparedStatement preparedStatement = ((Connection) service.getAccess()).prepareStatement(sql);
            final ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();
            final String result = resultSet.getString(1);

            resultSet.close();
            preparedStatement.close();
            return result;
        } catch (SQLException e) {
            throw new DaoException(e.getMessage());
        }
    }


    protected abstract String createCollectionSQL(String collectionName);

    protected abstract String addColumnSQL(String collectionName, String columnName, String columnType) throws DatabaseException;

    protected abstract String dropCollectionSQL(String collectionName);

    public void dropCollection(Service service, String collectionName) throws DaoException {
        final String sql = dropCollectionSQL(collectionName);
        execute(service, sql);
    }

    protected abstract String dropPrimaryConstraintSQL(String collectionName);

    public void dropPrimaryConstraint(Service service, String collectionName) throws DaoException {
        final String sql = dropPrimaryConstraintSQL(collectionName);
        execute(service, sql);
    }

    protected abstract String addPrimaryKeySQL(String collectionName, List<String> primaryKeyColumns);

    public void addPrimaryKey(Service service, String collectionName, List<String> primaryKeyColumns) throws DaoException {
        dropPrimaryConstraint(service, collectionName);
        final String sql = addPrimaryKeySQL(collectionName, primaryKeyColumns);
        execute(service, sql);
    }

    protected abstract String alterColumnSQL(String collectionName, String columnName, String columnType) throws DatabaseException;

    public void alterColumn(Service service, String collectionName, String columnName, String columnType) throws DaoException, DatabaseException {
        final String sql = alterColumnSQL(collectionName, columnName, columnType);
        execute(service, sql);
    }

    protected abstract String setNullableSQL(String collectionName, String columnName, boolean nullable) throws DatabaseException;

    public void setNullable(Service service, String collectionName, String columnName, boolean nullable) throws DaoException, DatabaseException {
        final String sql = setNullableSQL(collectionName, columnName, nullable);
        execute(service, sql);
    }

    protected abstract String createSequenceSQL(String sequenceName);

    public void createSequence(Service service, String sequenceName) throws DaoException {
        final String sql = createSequenceSQL(sequenceName);
        execute(service, sql);
    }

    protected abstract String setUniqueSQL(String collectionName, String columnName, boolean unique) throws DatabaseException;

    public void setUnique(Service service, String collectionName, String columnName, boolean unique) throws DaoException, DatabaseException {
        final String sql = setUniqueSQL(collectionName, columnName, unique);
        execute(service, sql);
    }

    public void createCollection(Service service, String collectionName, Object object) throws DaoException {
        final String createSql = createCollectionSQL(collectionName);
        final Field[] fields = Utils.getDeclaredFields(object);

        try {
            execute(service, createSql);
            for(Field field: fields) {
                final String addColumnSql = addColumnSQL(collectionName, Utils.getAnnotatedFieldName(field),
                        field.getType().getName());
                execute(service, addColumnSql);
                alterColumn(service, collectionName, Utils.getAnnotatedFieldName(field), field.getType().getName());

                if(field.isAnnotationPresent(mg.uniDao.annotation.Field.class)) {
                    mg.uniDao.annotation.Field annotation = field.getAnnotation(mg.uniDao.annotation.Field.class);
                    if(!annotation.isPrimaryKey()) {
                        setNullable(service, collectionName, Utils.getAnnotatedFieldName(field), annotation.isNullable());
                        setUnique(service, collectionName, Utils.getAnnotatedFieldName(field), annotation.isUnique());
                    }
                }

                if(field.isAnnotationPresent(mg.uniDao.annotation.AutoSequence.class)) {
                    mg.uniDao.annotation.AutoSequence annotation = field.getAnnotation(mg.uniDao.annotation.AutoSequence.class);
                    createSequence(service, annotation.name() + Config.SEQUENCE_SUFFIX);
                }
            }
            addPrimaryKey(service, collectionName, Utils.getPrimaryKeys(object).values().stream().toList());
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
