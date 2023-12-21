import mg.uniDao.core.Database;
import mg.uniDao.core.GenericDao;
import mg.uniDao.core.Service;
import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;
import mg.uniDao.provider.PostgresSql;
import mg.uniDao.test.Student;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DatabaseTest {
    Service getService() throws DatabaseException {
        Database postgresSql = new PostgresSql();
        return postgresSql.connect(false);
    }

    @Test
    void testPostgresSqlConnect() throws DatabaseException, DaoException {
        assertNotNull(getService().getAccess(), "PostgresSql connect() failed");
        getService().endConnection();
    }

    @Test
    void testCreateObject() throws DatabaseException, DaoException {
        Database postgresSql = new PostgresSql();
        Service service = postgresSql.connect();
        Student student = new Student();
        student.setAge(10);
        student.setName("Name");
        student.setSurname("Surname");
        postgresSql.create(service, "student", student);
        postgresSql.create(service, "student", student);
        service.endConnection();
    }

    @Test
    void testReadAll() throws DatabaseException, DaoException, ClassNotFoundException {
        Database postgresSql = new PostgresSql();
        Service service = postgresSql.connect();
        List<Student> studentList = postgresSql.readAll(service, "student", Student.class, 1, 10);
        studentList.size());
        System.out.println(new Student().readAllObject(service, 1, 10).size());
        service.endConnection();
    }
}
