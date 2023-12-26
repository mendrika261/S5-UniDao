import mg.uniDao.core.Database;
import mg.uniDao.core.Service;
import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;
import mg.uniDao.provider.PostgresSql;
import mg.uniDao.test.Student;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DatabaseTest {

    @Test
    void testPostgresSqlConnect() throws DatabaseException, DaoException {
        Database postgresSql = new PostgresSql();
        Service service = postgresSql.connect(false);
        assertNotNull(service.getAccess(), "PostgresSql connect() failed");
        service.endConnection();
    }

    @Test
    void testCreateObject() throws DatabaseException, DaoException {
        Database postgresSql = new PostgresSql();
        Service service = postgresSql.connect();
        Student student = new Student();
        student.setAge(10);
        student.setName("Name");
        student.setSurname("Surname");
        Assertions.assertThrows(DaoException.class, () -> postgresSql.create(service, "student", student));
        service.endConnection();
    }

    @Test
    void testFindList() throws DatabaseException, DaoException {
        Database postgresSql = new PostgresSql();
        Service service = postgresSql.connect();
        List<Student> studentList = postgresSql.findList(service, "student", Student.class,
                1, 10, "");
        assertEquals(studentList.size(), Student.findList(service, Student.class, 1, 10,
                "").size(), "ReadAll failed");
        service.endConnection();
    }

    @Test
    void testFind() throws DatabaseException, DaoException {
        Database postgresSql = new PostgresSql();
        Service service = postgresSql.connect();
        Student student = new Student();
        student.setAge(10);
        student = student.find(service, "");
        assertNotNull(student, "Read failed");
        service.endConnection();
    }

    @Test
    void testUpdate() throws DatabaseException, DaoException {
        Database postgresSql = new PostgresSql();
        Service service = postgresSql.connect();
        Student condition = new Student();
        condition.setAge(10);
        Student student = new Student();
        student.setAge(11);
        student.update(service, condition, "");
        service.endConnection();
    }
}
