package mg.uniDao.core;

import mg.uniDao.annotation.AutoSequence;
import mg.uniDao.exception.DaoException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.HashMap;

public class Utils {
    public static String upperFirst(String string) {
        return string.substring(0,1).toUpperCase()+string.substring(1);
    }

    private static Object getFieldValue(Object object, Field field) throws DaoException {
        Object fieldValue;
        String fieldName = field.getName();
        try {
            final Method getter = object.getClass().getMethod("get" + Utils.upperFirst(fieldName));
            fieldValue = getter.invoke(object);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            field.setAccessible(true);
            try {
                fieldValue = field.get(object);
            } catch (IllegalAccessException ignored2) {
                throw new DaoException("Cannot access field: " + fieldName + " in " + object.getClass().getName());
            }
        }
        return fieldValue;
    }

    public static void setFieldValue(Object object, Field field, Object value) throws DaoException {
        String fieldName = field.getName();
        try {
            final Method setter = object.getClass().getMethod("set" + Utils.upperFirst(fieldName), value.getClass());
            setter.invoke(object, value);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            field.setAccessible(true);
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw new DaoException("Cannot access field: " + fieldName + " in " + object.getClass().getName());
            }
        }
    }

    public static Field[] getDeclaredFields(Object object) {
        final Field[] superFields;
        /*if (object.getClass().isAnnotationPresent(AutoSequence.class))
            superFields = object.getClass().getSuperclass().getDeclaredFields();
        else*/
        superFields = new Field[0];
        final Field[] fields = object.getClass().getDeclaredFields();
        final Field[] allFields = Arrays.copyOf(superFields, superFields.length + fields.length);
        System.arraycopy(fields, 0, allFields, superFields.length, fields.length);
        return allFields;
    }

    public static HashMap<String, Object> getFieldsNamesWithValues(Object object) throws DaoException {
        final HashMap<String, Object> attributes = new HashMap<>();
        for(final Field field: getDeclaredFields(object)) {
            final String fieldName = field.getName();
            attributes.put(fieldName, getFieldValue(object, field));
        }
        return attributes;
    }

    public static String getAnnotatedFieldName(Field field) {
        if(field.isAnnotationPresent(mg.uniDao.annotation.Field.class))
            return field.getAnnotation(mg.uniDao.annotation.Field.class).name();
        return field.getName();
    }

    public static HashMap<String, Object> getFieldsAnnotatedNameWithValues(Object object) throws DaoException {
        final HashMap<String, Object> attributes = new HashMap<>();
        for(final Field field: getDeclaredFields(object)) {
            final String fieldName = getAnnotatedFieldName(field);
            attributes.put(fieldName, getFieldValue(object, field));
        }
        return attributes;
    }

    public static HashMap<String, Object> getFieldsNotNullAnnotatedNameWithValues(Object object, boolean throwPrimitiveType)
            throws DaoException {
        final HashMap<String, Object> attributes = new HashMap<>();
        final Field[] fields = getDeclaredFields(object);
        for(final Field field: fields) {
            if(throwPrimitiveType && field.getType().isPrimitive())
                throw new DaoException("Object condition work only with object without primitive field: \n" +
                        Arrays.toString(Arrays.stream(fields).toArray()));
            final String fieldName = getAnnotatedFieldName(field);
            final Object fieldValue = getFieldValue(object, field);
            if (fieldValue != null)
                attributes.put(fieldName, fieldValue);
        }
        return attributes;
    }

    public static HashMap<String, Object> getFieldsNotNullAnnotatedNameWithValues(Object object) throws DaoException {
        return getFieldsNotNullAnnotatedNameWithValues(object, false);
    }

    public static String fillSequence(String prefix, String value, int length) {
        return prefix + "0".repeat(Math.max(0, length - value.length() - prefix.length())) + value;
    }

    public static String getNextSequence(Service service, Field field) throws DaoException {
        final AutoSequence autoSequence = field.getAnnotation(AutoSequence.class);
        final String sequenceName = autoSequence.name() + Config.SEQUENCE_SUFFIX;
        return Utils.fillSequence(autoSequence.prefix(), service.getDatabase().getNextSequenceValue(service, sequenceName),
                autoSequence.length());
    }

    public static void fillAutoSequence(Service service, Object object) throws DaoException {
        final Field[] fields = Utils.getDeclaredFields(object);
        for(Field field: fields) {
            if(field.isAnnotationPresent(AutoSequence.class)) {
                final String nextSequence = getNextSequence(service, field);
                try {
                    Utils.setFieldValue(object, field, nextSequence);
                } catch (IllegalArgumentException e) {
                    throw new DaoException("Auto sequence field: '" + field.getName() + "' must be a String");
                }
            }
        }
    }

    public static HashMap<String, String> getPrimaryKeys(Object object) {
        final HashMap<String, String> primaryKeys = new HashMap<>();
        final Field[] fields = getDeclaredFields(object);
        for(Field field: fields) {
            if (field.isAnnotationPresent(mg.uniDao.annotation.Field.class)
                    && field.getAnnotation(mg.uniDao.annotation.Field.class).isPrimaryKey()) {
                primaryKeys.put(field.getName(), field.getAnnotation(mg.uniDao.annotation.Field.class).name());
            }
        }
        return primaryKeys;
    }
}
