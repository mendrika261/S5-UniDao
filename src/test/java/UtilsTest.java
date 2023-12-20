import mg.uniDao.core.Utils;
import mg.uniDao.exception.DaoException;
import mg.uniDao.test.Student;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilsTest {
    @Test
    void testGetAttributes() throws DaoException {
        Student student = new Student();
        student.setAge(10);
        student.setName("Name");
        student.setSurname("Surname");
        assertEquals("{surname=Surname, name=Name, age=10}",
                Utils.getAttributes(student).toString(), "Get attributes changed");
    }
}
