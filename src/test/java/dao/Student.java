package dao;

import mg.uniDao.annotation.AutoSequence;
import mg.uniDao.annotation.Collection;
import mg.uniDao.annotation.Field;
import mg.uniDao.core.GenericDao;

import java.time.LocalDate;

@Collection(name = "student", uniqueFields = {"name", "surname"})
public class Student extends GenericDao {
    @AutoSequence(name = "student", prefix = "ETU", length = 8)
    @Field(name = "id", isPrimaryKey = true)
    private String id;
    @Field(name = "name")
    private String name;
    private String surname;
    private LocalDate birthdate;
    private double average;
    private Formation formation;

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

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

}
