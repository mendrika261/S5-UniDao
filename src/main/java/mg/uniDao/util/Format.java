package mg.uniDao.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mg.uniDao.exception.DatabaseException;
import mg.uniDao.util.gsonAdapter.LocalDateAdapter;
import mg.uniDao.util.gsonAdapter.LocalDateTimeAdapter;

public class Format {
    public static Gson mapper;

    public static String upperFirst(String string) {
        return string.substring(0,1).toUpperCase()+string.substring(1);
    }

    public static String fillSequence(String prefix, String value, int length) {
        return prefix + "0".repeat(Math.max(0, length - value.length() - prefix.length())) + value;
    }

    static Gson getMapper() {
        if (mapper == null) {
            final GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter());
            builder.registerTypeAdapter(java.time.LocalDateTime.class, new LocalDateTimeAdapter());
            mapper = builder.create();
        }
        return mapper;
    }

    public static String toJson(Object object) {
        try {
            return getMapper().toJson(object);
        } catch (Exception e) {
            throw new DatabaseException("Cannot convert object to json: " + object.toString() + "\n" + e.getMessage());
        }
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        try {
            return getMapper().fromJson(json, classOfT);
        } catch (Exception e) {
            // Json is escaped from resultSet (because of getString() method)
            json = unescape(json.substring(1, json.length() - 1));
            return getMapper().fromJson(json, classOfT);
        }
    }

    public static String unescape(String string) {
        return string.replaceAll("\\\\\"", "");
    }

    public static String toSnakeCase(String text) {
        return text.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }
}
