import mg.uniDao.provider.GenericSqlProvider;
import mg.uniDao.util.FileUtils;
import mg.uniDao.util.Format;
import mg.uniDao.util.ObjectUtils;
import mg.uniDao.exception.DaoException;
import dao.Student;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectUtilsTest {
    @Test
    void testGetAttributesAnnotatedName() throws DaoException {
        Student student = new Student();
        student.setBirthdate(LocalDate.now());
        student.setName("John");
        student.setSurname("Doe");
        assertEquals("{surname=Doe, name=John, id=null, age=10}",
                ObjectUtils.getFieldsAnnotatedNameWithValues(student).toString(), "Get attributes changed");
    }

    @Test
    void testJsonFormat() {
        Student student = new Student();
        student.setBirthdate(LocalDate.now());
        student.setName("John");
        student.setSurname("Doe");

        String json = Format.toJson(student);
        Student student2 = Format.fromJson(json, Student.class);
        System.out.println(student2);
        assertEquals(student.toString(), student2.toString(), "Json format changed");
    }

    @Test
    void toJson() throws DaoException {
        System.out.println(Format.fromJson(FileUtils.getFileContentAsString("postgresql.json"),
                GenericSqlProvider.class));

    }
}
