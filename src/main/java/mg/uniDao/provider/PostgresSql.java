package mg.uniDao.provider;

import mg.uniDao.core.Database;
import mg.uniDao.core.Service;
import mg.uniDao.exception.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class PostgresSql<T> implements Database<T> {
    private static final String DRIVER = "org.postgresql.Driver";
    private static boolean DRIVER_LOADED = false;

    private void loadDriver() throws DatabaseException {
        if(!DRIVER_LOADED) {
            try {
                Class.forName(DRIVER);
            } catch (ClassNotFoundException e) {
                throw new DatabaseException("Cannot find the driver - " + DRIVER);
            }
            DRIVER_LOADED = true;
        }
    }

    private void verifyConnectionArguments(String... arguments) throws DatabaseException {
        if(arguments.length != 3)
            throw new DatabaseException("You must provide 3 arguments to the connection: url, username, password");
    }

    @Override
    public Service<?> connect(String... arguments) throws DatabaseException {
        verifyConnectionArguments(arguments);
        loadDriver();
        final Connection connection;
        try {
            connection = DriverManager.getConnection(
                    arguments[0], arguments[1], arguments[2]);
        } catch (SQLException e) {
            throw new DatabaseException("Credentials are not correct");
        }
        Service<Connection> service = new Service<>();
        service.setAccess(connection);
        return service;
    }

    @Override
    public void createObject(Service<?> service, Object object) {
    }

    @Override
    public List<T> readAllObject(Service<?> service, String tableName, Class<?> className) {
        return null;
    }

    @Override
    public T readObject(Service<?> service, String tableName, Class<?> className) {
        return null;
    }

    @Override
    public void updateObject(Service<?> service, Object object) {

    }
}
