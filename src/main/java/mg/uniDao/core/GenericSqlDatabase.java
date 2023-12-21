package mg.uniDao.core;

import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;
import mg.uniDao.test.Student;

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

    protected abstract String createSQL(String collectionName, HashMap<String, Object> attributes);

    @Override
    public void create(Service service, String collectionName, Object object) throws DaoException {
        final Connection connection = (Connection) service.getAccess();
        final HashMap<String, Object> attributes = Utils.getAttributes(object);
        final String sql = createSQL(collectionName, attributes);
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql);

            Method method;
            int i = 1;
            for (String key : attributes.keySet()) {
                method = Utils.getPreparedStatementSetter(attributes.get(key));
                method.invoke(preparedStatement, i, attributes.get(key));
                i++;
            }

            preparedStatement.executeUpdate();
            preparedStatement.close();
            if(!service.isTransactional())
                service.endConnection();
        } catch (SQLException | IllegalAccessException | InvocationTargetException e) {
            throw new DaoException(e.getMessage());
        }
    }

    protected abstract String readAllWithLimitSQL(String collectionName);

    @Override
    public <T> List<T> readAll(Service service, String collectionName, Class<T> className, int page, int limit) throws DaoException {
        final Connection connection = (Connection) service.getAccess();
        final String sql = readAllWithLimitSQL(collectionName);
        final List<T> objects = new ArrayList<T>();

        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, limit);
            preparedStatement.setInt(2, (page - 1) * limit);
            ResultSet resultSet = preparedStatement.executeQuery();
            Field[] methods = className.getDeclaredFields();

            while (resultSet.next()) {
                T object = className.getDeclaredConstructor().newInstance();
                for (Field method : methods) {
                    method.setAccessible(true);
                    method.set(object, resultSet.getObject(method.getName()));
                }
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

    @Override
    public <T> T read(Service service, String collectionName, Class<?> className) {
        return null;
    }

    @Override
    public void update(Service service, Object object) {
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
