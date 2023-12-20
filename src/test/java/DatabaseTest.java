import mg.uniDao.core.Config;
import mg.uniDao.core.Database;
import mg.uniDao.core.Service;
import mg.uniDao.core.Utils;
import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;
import mg.uniDao.provider.PostgresSql;
import mg.uniDao.test.Student;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DatabaseTest {
    Service getService() throws DatabaseException {
        Database<?> postgresSql = new PostgresSql<>();
        return postgresSql.connect(false);
    }

    @Test
    void testPostgresSqlConnect() throws DatabaseException, DaoException {
        assertNotNull(getService().getAccess(), "PostgresSql connect() failed");
    }

    @Test
    void testCreateObject() throws DatabaseException, DaoException {
        Database<?> postgresSql = new PostgresSql<>();
        Service service = postgresSql.connect(false);
        Student student = new Student();
        student.setAge(10);
        student.setName("Name");
        student.setSurname("Surname");
        postgresSql.createObject(service, "student", student);
        postgresSql.createObject(service, "student", student);
    }
}
