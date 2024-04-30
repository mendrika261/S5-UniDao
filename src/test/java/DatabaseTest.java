import dao.Formation;
import dao.Mark;
import mg.uniDao.core.Database;
import mg.uniDao.core.Service;
import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;
import mg.uniDao.provider.GenericSqlProvider;
import dao.Student;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DatabaseTest {

    @Test
    void testPostgresSqlConnect() throws DaoException {
        Database postgresSql = GenericSqlProvider.get("postgresql.json");

        Service service = postgresSql.connect("TEST", true);

        List<Region> regions = new Region().getList(service);
        System.out.println(regions);

        service.endConnection();

        /*Service service = postgresSql.connect("TEST", false);
        assertNotNull(service.getAccess(), "PostgresSql connect() failed");
        service.endConnection();

        Service service2 = postgresSql.connect("PROD", false);
        assertNotNull(service2.getAccess(), "PostgresSql connect() failed");
        service2.endConnection();*/
    }

    @Test
    void testCreateCollection() throws DatabaseException, DaoException {
        Database postgresSql = new GenericSqlProvider();
        Service service = postgresSql.connect();
        Student student = new Student();
        student.createCollection(service);
        Mark mark = new Mark();
        mark.createCollection(service);
        service.endConnection();
    }

    @Test
    void testCreateObject() throws DatabaseException, DaoException {
        Database postgresSql = new GenericSqlProvider();
        Service service = postgresSql.connect();
        Student student = new Student();
        student.setBirthdate(LocalDate.now());
        student.setInscriptionDate(LocalDateTime.now());
        student.setName("Name");
        student.setSurname("Surname");

        Formation formation = new Formation();
        formation.setName("Formation");
        student.setFormation(formation);
        student.save(service);

        Mark mark = new Mark();
        mark.setCoefficient(1);
        mark.setValue(10.0);
        mark.setStudent(student);
        mark.save(service);

        service.endConnection();
    }

    @Test
    void testFindList() throws DatabaseException, DaoException {
        Database postgresSql = new GenericSqlProvider();
        Service service = postgresSql.connect();
        List<Mark> studentList = postgresSql.findList(service, Mark.class,
                1, 10, new String[]{"student"});
        System.out.println(studentList);
        service.endConnection();
    }

    @Test
    void testFind() throws DatabaseException, DaoException {
        Database postgresSql = new GenericSqlProvider();
        Service service = postgresSql.connect();
        Mark mark = new Mark();
        mark.setCoefficient(1);
        mark = mark.get(service, "student");
        System.out.println(mark);
        assertNotNull(mark, "Read failed");
        service.endConnection();
    }

    @Test
    void testUpdate() throws DatabaseException, DaoException {
        Database postgresSql = new GenericSqlProvider();
        Service service = postgresSql.connect();
        Student condition = new Student();
        condition.setBirthdate(LocalDate.now());
        Student student = new Student();
        student.setBirthdate(LocalDate.now());
        student.update(service, condition);
        service.endConnection();
    }

    @Test
    void testDelete() throws DatabaseException, DaoException {
        Database postgresSql = new GenericSqlProvider();
        Service service = postgresSql.connect();
        Student student = new Student();
        student.setId("30");
        student.delete(service, "");
        service.endConnection();
    }
}
