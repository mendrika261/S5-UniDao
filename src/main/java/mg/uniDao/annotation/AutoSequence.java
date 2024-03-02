package mg.uniDao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>This annotation is used to mark a field as an auto sequence field.</p>
 * <b>Note:</b> The field is expected to be a String.
 *
 * @name the name of the sequence in the database, default is the name with "seq" appended
 * @length the length of the sequence, default is auto
 * @prefix the prefix of the sequence, default is none
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoSequence {
    /**
     * @return The name of the sequence in the database, default is the name with "seq" appended
     */
    String name();
    /**
     * @return The length of the sequence, default is auto
     */
    int length() default 0;
    /**
     * @return The prefix of the sequence, default is none
     */
    String prefix() default "";
}
