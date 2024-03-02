package dao;

import mg.uniDao.annotation.AutoSequence;
import mg.uniDao.annotation.Collection;
import mg.uniDao.annotation.Field;
import mg.uniDao.annotation.Reference;
import mg.uniDao.core.sql.GenericSqlDao;

@Collection(name = "mark")
public class Mark extends GenericSqlDao {
    @Field(name = "id", isPrimaryKey = true)
    @AutoSequence(name = "mark")
    String id;
    @Field(name = "student_id")
    @Reference
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

    public void setId(String id) {
        this.id = id;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public void setCoefficient(int coefficient) {
        this.coefficient = coefficient;
    }
}
