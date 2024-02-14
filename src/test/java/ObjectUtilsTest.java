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
}
