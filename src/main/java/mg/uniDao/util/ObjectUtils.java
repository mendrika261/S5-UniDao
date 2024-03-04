package mg.uniDao.util;

import mg.uniDao.annotation.AutoSequence;
import mg.uniDao.annotation.Collection;
import mg.uniDao.annotation.Reference;
import mg.uniDao.core.Service;
import mg.uniDao.core.sql.GenericSqlDao;
import mg.uniDao.exception.DaoException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ObjectUtils {

    private static Object getFieldValue(Object object, Field field) throws DaoException {
        Object fieldValue;
        String fieldName = field.getName();
        try {
            final Method getter = object.getClass().getMethod("get" + Format.upperFirst(fieldName));
            fieldValue = getter.invoke(object);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            field.setAccessible(true);
            try {
                fieldValue = field.get(object);
            } catch (IllegalAccessException ignored2) {
                throw new DaoException("Can not access field: " + fieldName + " in " + object.getClass().getName());
            }
        }
        return fieldValue;
    }

    private static Object getRealValue(Field field, Object value) {
        try {
            if(field.getType() == LocalDate.class)
                return ((Date) value).toLocalDate();
            if(field.getType() == LocalDateTime.class)
                return ((java.sql.Timestamp) value).toLocalDateTime();
            return field.getType().cast(value);
        } catch (ClassCastException e) {
            final String json = Format.toJson(value);
            return Format.fromJson(json, field.getType());
        }
    }

    public static void setFieldValue(Object object, Field field, Object value) throws DaoException {
        String fieldName = field.getName();
        Object castedValue = getRealValue(field, value);
        try {
            final Method setter = object.getClass().getMethod("set" + Format.upperFirst(fieldName), castedValue.getClass());
            setter.invoke(object, castedValue);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            field.setAccessible(true);
            try {
                field.set(object, castedValue);
            } catch (IllegalAccessException e) {
                throw new DaoException("Can not access field: " + fieldName + " in " + object.getClass().getName());
            }
        }
    }

    public static Field[] getDeclaredFields(Class<?> objectClass) {
        final Field[] superFields;
        /*if (object.getClass().isAnnotationPresent(AutoSequence.class))
            superFields = object.getClass().getSuperclass().getDeclaredFields();
        else*/
        superFields = new Field[0];
        final Field[] fields = objectClass.getDeclaredFields();
        final Field[] allFields = Arrays.copyOf(superFields, superFields.length + fields.length);
        System.arraycopy(fields, 0, allFields, superFields.length, fields.length);
        return allFields;
    }

    // field name: field value
    public static HashMap<String, Object> getFieldsNamesWithValues(Object object) throws DaoException {
        final HashMap<String, Object> attributes = new HashMap<>();
        for(final Field field: getDeclaredFields(object.getClass())) {
            final String fieldName = field.getName();
            attributes.put(fieldName, getFieldValue(object, field));
        }
        return attributes;
    }

    public static HashMap<String, Object> getFieldsNamesWithValuesNonNull(Object object) throws DaoException {
        final HashMap<String, Object> attributes = new HashMap<>();
        for(final Field field: getDeclaredFields(object.getClass())) {
            final String fieldName = field.getName();
            Object fieldValue = getFieldValue(object, field);
            if (fieldValue != null)
                attributes.put(fieldName, fieldValue);
        }
        return attributes;
    }

    public static String getAnnotatedFieldName(Field field) {
        if(field.isAnnotationPresent(mg.uniDao.annotation.Field.class)
                && !field.getAnnotation(mg.uniDao.annotation.Field.class).name().isEmpty())
            return field.getAnnotation(mg.uniDao.annotation.Field.class).name();
        return field.getName();
    }

    public static HashMap<Field, Object> getFieldsAnnotatedNameWithValues(Object object) throws DaoException {
        final HashMap<Field, Object> attributes = new HashMap<>();
        for(final Field field: getDeclaredFields(object.getClass())) {
            if(field.isAnnotationPresent(Reference.class)) {
                Optional<String> principalKey = ObjectUtils.getPrimaryKeys(field.getType())
                        .keySet().stream().findFirst();
                Object fieldObject = getFieldValue(object, field);
                attributes.put(field, getFieldValue(fieldObject, ObjectUtils.getDeclaredField(fieldObject.getClass(),
                        principalKey.orElseThrow())));
            } else
                attributes.put(field, getFieldValue(object, field));
        }
        return attributes;
    }

    public static HashMap<Field, Object> getFieldsNotNullAnnotatedNameWithValues(Object object, boolean throwPrimitiveType)
            throws DaoException {
        final HashMap<Field, Object> attributes = new HashMap<>();

        if (object == null)
            return attributes;

        final Field[] fields = getDeclaredFields(object.getClass());
        for(final Field field: fields) {
            if(throwPrimitiveType && field.getType().isPrimitive())
                throw new DaoException("Object condition work only with object without primitive field: \n" +
                        Arrays.toString(Arrays.stream(fields).toArray()));
            //final String fieldName = getAnnotatedFieldName(field);
            final Object fieldValue = getFieldValue(object, field);
            if (fieldValue != null)
                attributes.put(field, fieldValue);
        }
        return attributes;
    }

    public static HashMap<Field, Object> getFieldsNotNullAnnotatedNameWithValues(Object object) throws DaoException {
        return getFieldsNotNullAnnotatedNameWithValues(object, false);
    }

    public static String getNextSequence(Service service, Field field) throws DaoException {
        final AutoSequence autoSequence = field.getAnnotation(AutoSequence.class);
        final String sequenceName = autoSequence.name() + Config.SEQUENCE_SUFFIX;
        return Format.fillSequence(autoSequence.prefix(), service.getDatabase().getNextSequenceValue(service, sequenceName),
                autoSequence.length());
    }

    public static void fillAutoSequence(Service service, Object object) throws DaoException {
        final Field[] fields = ObjectUtils.getDeclaredFields(object.getClass());
        for(Field field: fields) {
            if(field.isAnnotationPresent(AutoSequence.class)) {
                final String nextSequence = getNextSequence(service, field);
                try {
                    ObjectUtils.setFieldValue(object, field, nextSequence);
                } catch (IllegalArgumentException e) {
                    throw new DaoException("Auto sequence field: '" + field.getName() + "' must be a String");
                }
            }
        }
    }

    // field name: annotated field name
    public static HashMap<String, String> getPrimaryKeys(Class<?> objectClass) throws DaoException {
        final HashMap<String, String> primaryKeys = new HashMap<>();
        final Field[] fields = getDeclaredFields(objectClass);
        for(Field field: fields) {
            if (field.isAnnotationPresent(mg.uniDao.annotation.Field.class)
                    && field.getAnnotation(mg.uniDao.annotation.Field.class).isPrimaryKey()) {
                primaryKeys.put(field.getName(), ObjectUtils.getAnnotatedFieldName(field));
            }
        }
        if (primaryKeys.isEmpty())
            throw new DaoException("No primary key found in " + objectClass.getName() + ", " +
                    "use @Link instead of @Collection if it is a linking collection");
        return primaryKeys;
    }

    // field name: value
    public static HashMap<String, Object> getPrimaryKeysWithValue(Object object) throws DaoException {
        final HashMap<String, Object> primaryKeys = new HashMap<>();
        final Field[] fields = getDeclaredFields(object.getClass());
        for(Field field: fields) {
            if (field.isAnnotationPresent(mg.uniDao.annotation.Field.class)
                    && field.getAnnotation(mg.uniDao.annotation.Field.class).isPrimaryKey()) {
                primaryKeys.put(field.getName(), ObjectUtils.getFieldValue(object, field));
            }
        }
        if (primaryKeys.isEmpty())
            throw new DaoException("No primary key found in " + object.getClass().getName() + ", " +
                    "use @Link instead of @Collection if it is a linking collection");
        return primaryKeys;
    }


    public static Field getDeclaredField(Class<?> className, String fieldName) throws DaoException {
        try {
            return className.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new DaoException("Field: " + fieldName + " not found in " + className.getName());
        }
    }

    public static List<String> getColumnNames(Class<?> objectClass) {
        return Arrays.asList(Arrays.stream(getDeclaredFields(objectClass)).map(ObjectUtils::getAnnotatedFieldName)
                .toArray(String[]::new));
    }

    public static List<String> getColumnNamesWithChildren(Class<?> objectClass, String prefix) {
        Field[] fields = getDeclaredFields(objectClass);
        List<String> columnNames = new ArrayList<>();
        StringBuilder prefixBuilder = new StringBuilder(prefix);
        for(Field field: fields) {
            if(field.isAnnotationPresent(Reference.class)) {
                Class<?> fieldClass = field.getType();
                prefixBuilder.append(field.getName()).append(".");
                columnNames.addAll(getColumnNamesWithChildren(fieldClass, prefixBuilder.toString()));
            } else {
                columnNames.add(prefix+getAnnotatedFieldName(field));
            }
        }
        return columnNames;
    }

    public static String getCollectionName(Class<?> objectClass) throws DaoException {
        if (!objectClass.isAnnotationPresent(mg.uniDao.annotation.Collection.class))
            return objectClass.getSimpleName();
            /*throw new DaoException(objectClass.getSimpleName() + " is not a collection, " +
                    "it should be annotated with @Collection to inherit methods.");*/
        if (!objectClass.getAnnotation(mg.uniDao.annotation.Collection.class).name().isEmpty())
            return objectClass.getAnnotation(Collection.class).name();
        return objectClass.getSimpleName();
    }

    public static void fillPrimaryKeys(Object condition, Object object) {
        final Field[] fields = getDeclaredFields(condition.getClass());
        for(Field field: fields) {
            if (field.isAnnotationPresent(mg.uniDao.annotation.Field.class)
                    && field.getAnnotation(mg.uniDao.annotation.Field.class).isPrimaryKey()) {
                try {
                    ObjectUtils.setFieldValue(condition, field, ObjectUtils.getFieldValue(object, field));
                } catch (DaoException ignored) {}
            }
        }
    }

    public static String getId(Object object) {
        final Field[] fields = getDeclaredFields(object.getClass());
        for(Field field: fields) {
            if (field.isAnnotationPresent(mg.uniDao.annotation.Field.class)
                    && field.getAnnotation(mg.uniDao.annotation.Field.class).isPrimaryKey()) {
                try {
                    return (String) ObjectUtils.getFieldValue(object, field);
                } catch (DaoException ignored) {}
            }
        }
        return null;
    }
}
