package mg.uniDao.core;

import mg.uniDao.exception.DatabaseException;

public class Service<T> {
    private T access;

    public Service() {
    }

    public T getAccess() {
        return access;
    }

    public void setAccess(T access) throws DatabaseException {
        if(access == null)
            throw new DatabaseException("Cannot have access to database with the given access");
        this.access = access;
    }
}
