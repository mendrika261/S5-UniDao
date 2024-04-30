# Overview

The goal of this project is to create a cross-database Data Access Object, with database generation and migration capabilities.
> It is used with [this web framework](https://github.com/mendrika261/S4-Java-Framework)

## Example of usage
```java
package project.entity;

import mg.uniDao.annotation.Field;
import mg.uniDao.annotation.AutoSequence;
import mg.uniDao.core.sql.GenericSqlDao;
import mg.uniDao.annotation.Collection;


@Collection
public class Region extends GenericSqlDao {
	@Field(name = "region_id", isPrimaryKey = true)
	@AutoSequence(name = "region")
	private Integer regionId;
	@Field(name = "region_description")
	private String regionDescription;
```

## Getting Started for dev

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Installing

1. Clone the repository to your local machine.
2. Run `mvn clean install` to install dependencies.
3. (For building jar) Run `mvn package -DskipTests=true` to build the project.

## Built With

- [Java](https://www.java.com/) - The programming language used
- [Maven](https://maven.apache.org/) - Dependency Management
- [JUnit](https://junit.org/junit5/) - Testing framework
- [Gson](https://mvnrepository.com/artifact/com.google.code.gson/gson) - JSON library for Java
- [DotEnv](https://mvnrepository.com/artifact/io.github.cdimascio/java-dotenv) - Java dot env