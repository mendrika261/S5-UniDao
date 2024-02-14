package mg.uniDao.util;

public class Format {
    public static String upperFirst(String string) {
        return string.substring(0,1).toUpperCase()+string.substring(1);
    }

    public static String fillSequence(String prefix, String value, int length) {
        return prefix + "0".repeat(Math.max(0, length - value.length() - prefix.length())) + value;
    }
}
