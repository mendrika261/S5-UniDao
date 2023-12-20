package mg.uniDao.test;

import mg.uniDao.core.GenericDao;
import mg.uniDao.core.Utils;
import mg.uniDao.exception.DaoException;

public class Student extends GenericDao {
    private String name;
    private String surname;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        try {
            return String.valueOf(Utils.getAttributes(this));
        } catch (DaoException e) {
            throw new RuntimeException(e);
        }
    }
}
