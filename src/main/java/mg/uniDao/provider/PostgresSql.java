package mg.uniDao.provider;

import mg.uniDao.core.Database;
import mg.uniDao.core.GenericDatabase;
import mg.uniDao.core.Service;
import mg.uniDao.exception.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class PostgresSql<T> extends GenericDatabase<T> {
}
