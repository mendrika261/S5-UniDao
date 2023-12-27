package dao;

import mg.uniDao.annotation.AutoSequence;
import mg.uniDao.annotation.Collection;
import mg.uniDao.annotation.Field;
import mg.uniDao.core.GenericDao;

@Collection(name = "student")
public class Student extends GenericDao {
    @AutoSequence(prefix = "ETU", length = 8)
    @Field(name = "id2")
    private String id;

    @Field(name = "nam")
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
}
