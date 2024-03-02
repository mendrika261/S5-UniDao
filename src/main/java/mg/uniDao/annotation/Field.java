package mg.uniDao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>This annotation is used to customize fields in a collection.</p>
 *
 * @name the name of the field in the database, default is property name
 * @isPrimaryKey if the field is a primary key, default is false
 * @isNullable if the field is nullable, default is true
 * @isUnique if the field is unique, default is false
 * @databaseMappingType the type of the field in the database, default is from the database provider
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Field {
    /**
     * @return the name of the field in the database, default is property name
     */
    String name() default "";
    /**
     * @return if the field is a primary key, default is false
     */
    boolean isPrimaryKey() default false;
    /**
     * @return if the field is nullable, default is true
     */
    boolean isNullable() default true;
    /**
     * @return if the field is unique, default is false
     */
    boolean isUnique() default false;
    /**
     * @return the type of the field in the database, default is from the database provider
     */
    String databaseMappingType() default "";
}
