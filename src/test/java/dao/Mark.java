package dao;

import mg.uniDao.annotation.AutoSequence;
import mg.uniDao.annotation.Collection;
import mg.uniDao.annotation.Field;
import mg.uniDao.annotation.Reference;
import mg.uniDao.core.GenericDao;

@Collection(name = "mark")
public class Mark extends GenericDao {
    @Field(name = "id", isPrimaryKey = true)
    @AutoSequence(name = "mark")
    String id;
    @Field(name = "student_id")
    @Reference(collection=Student.class, fields={"id"})
    Student student;
    Double value;
    int coefficient;

    public String getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public Double getValue() {
        return value;
    }

    public int getCoefficient() {
        return coefficient;
    }
}
