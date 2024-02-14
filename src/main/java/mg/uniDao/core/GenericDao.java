package mg.uniDao.core;

import mg.uniDao.annotation.Collection;
import mg.uniDao.annotation.AutoSequence;

import mg.uniDao.exception.DaoException;
import mg.uniDao.exception.DatabaseException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

public class GenericDao {
    private final HashMap<String, String> primaryKeys = new HashMap<>();

    public GenericDao() {
        final Field[] fields = Utils.getDeclaredFields(this);
        for(Field field: fields) {
            if (field.isAnnotationPresent(mg.uniDao.annotation.Field.class)
                    && field.getAnnotation(mg.uniDao.annotation.Field.class).isPrimaryKey()) {
                primaryKeys.put(field.getName(), field.getAnnotation(mg.uniDao.annotation.Field.class).name());
            }
        }
    }

    private String getCollectionName() {
        if (getClass().isAnnotationPresent(Collection.class))
            return getClass().getAnnotation(Collection.class).name();
        return getClass().getSimpleName().toLowerCase();
    }

    private String getNextSequence(Service service, Field field) throws DaoException {
        final AutoSequence autoSequence = field.getAnnotation(AutoSequence.class);
        final String sequenceName = autoSequence.name().isEmpty() ? getCollectionName() + "_seq" : autoSequence.name();
        return Utils.fillSequence(autoSequence.prefix(), service.getDatabase().getNextSequenceValue(service, sequenceName), autoSequence.length());
    }

    private void fillAutoSequence(Service service) throws DaoException {
        final Field[] fields = Utils.getDeclaredFields(this);
        for(Field field: fields) {
            if(field.isAnnotationPresent(AutoSequence.class)) {
                final String nextSequence = getNextSequence(service, field);
                try {
                    Utils.setFieldValue(this, field, nextSequence);
                } catch (IllegalArgumentException e) {
                    throw new DaoException("Auto sequence field: '" + field.getName() + "' must be a String");
                }
            }
        }
    }

    public void save(Service service) throws DaoException {
        fillAutoSequence(service);
        service.getDatabase().create(service, getCollectionName(), this);
    }

    public <T> List<T> findList(Service service, int page, int limit, String extraCondition)
            throws DaoException {
        return service.getDatabase().findList(service, getCollectionName(), (Class<T>) getClass(), page, limit, extraCondition);
    }

    public <T> T find(Service service, String extraCondition) throws DaoException {
        return service.getDatabase().find(service, getCollectionName(), this, extraCondition);
    }

    public void update(Service service, Object condition, String extraCondition) throws DaoException {
        service.getDatabase().update(service, getCollectionName(), condition, this, extraCondition);
    }

    public void delete(Service service, String extraCondition) throws DaoException {
        service.getDatabase().delete(service, getCollectionName(), this, extraCondition);
    }


    public void createCollection(Service service) throws DaoException, DatabaseException {
        service.getDatabase().createCollection(service, getCollectionName(), this);
        addPrimaryKey(service);
    }

    private void addPrimaryKey(Service service) throws DaoException {
        service.getDatabase().addPrimaryKey(service, getCollectionName(), primaryKeys.values().stream().toList());
    }


    @Override
    public String toString() {
        try {
            return String.valueOf(Utils.getFieldsNamesWithValues(this));
        } catch (DaoException ignored) {}
        return null;
    }
}
