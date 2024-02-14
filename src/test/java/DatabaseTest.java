import mg.uniDao.core.Database;
import mg.uniDao.core.Service;
import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;
import mg.uniDao.provider.PostgresSql;
import dao.Student;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
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
    void testCreateCollection() throws DatabaseException, DaoException {
        Database postgresSql = new PostgresSql();
        Service service = postgresSql.connect();
        Student student = new Student();
        student.createCollection(service);
        service.endConnection();
    }

    @Test
    void testCreateObject() throws DatabaseException, DaoException {
        Database postgresSql = new PostgresSql();
        Service service = postgresSql.connect();
        Student student = new Student();
        student.setBirthdate(LocalDate.now());
        student.setName("Name");
        student.setSurname("Surname");
        student.save(service);
        service.endConnection();
    }

    @Test
    void testFindList() throws DatabaseException, DaoException {
        Database postgresSql = new PostgresSql();
        Service service = postgresSql.connect();
        List<Student> studentList = postgresSql.findList(service, "student", Student.class,
                1, 10, "");
        System.out.println(studentList);
        assertEquals(studentList.size(), new Student().findList(service, 1, 10, "").size(),
                "ReadAll failed");
        service.endConnection();
    }

    @Test
    void testFind() throws DatabaseException, DaoException {
        Database postgresSql = new PostgresSql();
        Service service = postgresSql.connect();
        Student student = new Student();
        student.setBirthdate(LocalDate.now());
        student = student.find(service, "");
        System.out.println(student);
        assertNotNull(student, "Read failed");
        service.endConnection();
    }

    @Test
    void testUpdate() throws DatabaseException, DaoException {
        Database postgresSql = new PostgresSql();
        Service service = postgresSql.connect();
        Student condition = new Student();
        condition.setBirthdate(LocalDate.now());
        Student student = new Student();
        student.setBirthdate(LocalDate.now());
        student.update(service, condition, "");
        service.endConnection();
    }

    @Test
    void testDelete() throws DatabaseException, DaoException {
        Database postgresSql = new PostgresSql();
        Service service = postgresSql.connect();
        Student student = new Student();
        student.setId("30");
        student.delete(service, "");
        service.endConnection();
    }
}
