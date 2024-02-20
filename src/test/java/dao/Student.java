package dao;

import mg.uniDao.annotation.AutoSequence;
import mg.uniDao.annotation.Collection;
import mg.uniDao.annotation.Field;
import mg.uniDao.core.sql.GenericSqlDao;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Collection(name = "student", uniqueFields = {"name", "surname"})
public class Student extends GenericSqlDao {
    @AutoSequence(name = "student", prefix = "ETU", length = 8)
    @Field(name = "id", isPrimaryKey = true)
    private String id;
    @Field(name = "name")
    private String name;
    private String surname;
    private LocalDate birthdate;
    private LocalDateTime inscriptionDate;
    private double average;
    private Formation formation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public LocalDateTime getInscriptionDate() {
        return inscriptionDate;
    }

    public void setInscriptionDate(LocalDateTime inscriptionDate) {
        this.inscriptionDate = inscriptionDate;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public Formation getFormation() {
        return formation;
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
    }
}
