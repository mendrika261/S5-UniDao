package mg.uniDao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>This annotation is used to mark a class as a collection.</p>
 *
 * @name the name of the collection in the database, default is the class name
 * @uniqueFields the fields that are unique in the collection
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Collection {
    /**
     * @return The name of the collection in the database, default is the class name
     */
    String name() default "";
    /**
     * @return The fields that are unique in the collection
     */
    String[] uniqueFields() default {};

    /**
     * @return The database config name in environnement
     */
    boolean historize() default false;
}
