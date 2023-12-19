package mg.uniDao.core;

import java.sql.Connection;
import java.util.List;

public class GenericDao {
    public void createObject(Connection connection) {
    }

    public static <T> List<T> readAllObject(Connection connection) {
        return null;
    }

    public static <T> T readObject(Connection connection) {
        return null;
    }

    public void updateObject(Connection connection) {
    }
}
