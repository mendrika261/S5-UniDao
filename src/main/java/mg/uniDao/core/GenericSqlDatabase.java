package mg.uniDao.core;

import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public abstract class GenericSqlDatabase<T> implements Database<T> {
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
        return new Service(connection, transaction);
    }

    protected abstract String createObjectSQL(String collectionName, HashMap<String, Object> attributes);

    @Override
    public void createObject(Service service, String collectionName, Object object) throws DaoException {
        final Connection connection = (Connection) service.getAccess();
        final HashMap<String, Object> attributes = Utils.getAttributes(object);
        final String sql = createObjectSQL(collectionName, attributes);
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
            throw new DaoException("Cannot create object: " + object.getClass().getSimpleName() + attributes);
        }
    }


    @Override
    public List<T> readAllObject(Service service, String tableName, Class<?> className) {
        return null;
    }

    @Override
    public T readObject(Service service, String tableName, Class<?> className) {
        return null;
    }

    @Override
    public void updateObject(Service service, Object object) {
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
