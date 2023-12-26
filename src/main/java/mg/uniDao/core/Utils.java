package mg.uniDao.core;

import mg.uniDao.exception.DaoException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.HashMap;

public class Utils {
    public static String upperFirst(String string) {
        return string.substring(0,1).toUpperCase()+string.substring(1);
    }

    private static Object getFieldValue(Object object, String fieldName) throws DaoException {
        Object fieldValue;
        try {
            Method getter = object.getClass().getMethod("get" + Utils.upperFirst(fieldName));
            fieldValue = getter.invoke(object);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            Field field;
            try {
                field = object.getClass().getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored1) {
                throw new DaoException("Cannot find field: " + fieldName + " in " + object.getClass().getName());
            }
            field.setAccessible(true);
            try {
                fieldValue = field.get(object);
            } catch (IllegalAccessException ignored2) {
                throw new DaoException("Cannot access field: " + fieldName + " in " + object.getClass().getName());
            }
        }
        return fieldValue;
    }

    public static HashMap<String, Object> getAttributes(Object object) throws DaoException {
        HashMap<String, Object> attributes = new HashMap<>();
        for(final Field field: object.getClass().getDeclaredFields()) {
            final String fieldName = field.getName();
            attributes.put(fieldName, getFieldValue(object, fieldName));
        }
        return attributes;
    }

    public static String getAnnotatedFieldName(Field field) {
        if(field.isAnnotationPresent(mg.uniDao.annotation.Field.class))
            return field.getAnnotation(mg.uniDao.annotation.Field.class).name();
        return field.getName();
    }

    public static HashMap<String, Object> getAttributesAnnotatedName(Object object) throws DaoException {
        HashMap<String, Object> attributes = new HashMap<>();
        for(final Field field: object.getClass().getDeclaredFields()) {
            final String fieldName = getAnnotatedFieldName(field);
            attributes.put(fieldName, getFieldValue(object, field.getName()));
        }
        return attributes;
    }

    public static HashMap<String, Object> getAttributesNotNullAnnotatedName(Object object) throws DaoException {
        HashMap<String, Object> attributes = new HashMap<>();
        for(final Field field: object.getClass().getDeclaredFields()) {
            final String fieldName = getAnnotatedFieldName(field);
            Object fieldValue = getFieldValue(object, field.getName());
            if (fieldValue != null)
                attributes.put(fieldName, fieldValue);
        }
        return attributes;
    }


    public static Method getPreparedStatementSetter(Object object) throws DaoException {
        Class<?> objectClass = object.getClass();
        try {
            return switch (objectClass.getSimpleName()) {
                case "Integer" -> PreparedStatement.class.getMethod("setInt", int.class, int.class);
                case "Double" -> PreparedStatement.class.getMethod("setDouble", int.class, double.class);
                case "Boolean" -> PreparedStatement.class.getMethod("setBoolean", int.class, boolean.class);
                default -> PreparedStatement.class.getMethod("set" +
                        Utils.upperFirst(objectClass.getSimpleName()), int.class, objectClass);
            };
        } catch (NoSuchMethodException e) {
            throw new DaoException(objectClass.getName()
                    + " is not a valid type of attribute for a dao object OR not yet supported");
        }
    }
}
