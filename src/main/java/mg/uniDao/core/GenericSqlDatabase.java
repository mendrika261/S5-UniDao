package mg.uniDao.core;

import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    public GenericSqlDatabase(String url, String username, String password) {
        setUrl(url);
        setUsername(username);
        setPassword(password);
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
            throws IllegalAccessException, InvocationTargetException, DaoException {
        int i = start;
        for (String key : attributes.keySet()) {
            final Method preparedStatementSetter = Utils.getPreparedStatementSetter(attributes.get(key));
            preparedStatementSetter.invoke(preparedStatement, i, attributes.get(key));
            i++;
        }
    }

    protected abstract String createSQL(String collectionName, HashMap<String, Object> attributes);

    @Override
    public void create(Service service, String collectionName, Object object) throws DaoException {
        final Connection connection = (Connection) service.getAccess();
        final HashMap<String, Object> attributes = Utils.getAttributes(object);
        final String sql = createSQL(collectionName, attributes);
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql);
            prepareStatement(preparedStatement, attributes, 1);

            preparedStatement.executeUpdate();
            preparedStatement.close();
            if(!service.isTransactional())
                service.endConnection();
        } catch (SQLException | IllegalAccessException | InvocationTargetException e) {
            throw new DaoException(e.getMessage());
        }
    }

    private <T> T resultSetToObject(ResultSet resultSet, Class<T> className) throws SQLException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        final Field[] fields = className.getDeclaredFields();
        final T object = className.getDeclaredConstructor().newInstance();
        for (Field field : fields) {
            field.setAccessible(true);
            field.set(object, resultSet.getObject(field.getName()));
        }
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
        final HashMap<String, Object> conditions = Utils.getAttributesNotNull(condition);
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
        final Connection connection = (Connection) service.getAccess();
        final HashMap<String, Object> values = Utils.getAttributesNotNull(object);
        final HashMap<String, Object> conditions = Utils.getAttributesNotNull(condition);
        final String sql = updateSQL(collectionName, values, conditions, extraCondition);

        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql);
            prepareStatement(preparedStatement, values, 1);
            prepareStatement(preparedStatement, conditions, values.size()+1);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            if(!service.isTransactional())
                service.endConnection();
        } catch (SQLException | IllegalAccessException | InvocationTargetException e) {
            throw new DaoException(e.getMessage());
        }
    }

    protected abstract String deleteSQL(String collectionName, HashMap<String, Object> conditions, String extraCondition);

    @Override
    public void delete(Service service, String collectionName, Object condition, String extraCondition) throws DaoException {
        final Connection connection = (Connection) service.getAccess();
        final HashMap<String, Object> conditions = Utils.getAttributesNotNull(condition);
        final String sql = deleteSQL(collectionName, conditions, extraCondition);

        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql);
            prepareStatement(preparedStatement, conditions, 1);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            if(!service.isTransactional())
                service.endConnection();
        } catch (SQLException | IllegalAccessException | InvocationTargetException e) {
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
