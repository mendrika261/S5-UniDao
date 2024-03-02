package mg.uniDao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>This annotation is used to mark a field as a reference to another collection.</p>
 * <b>Note:</b> The field is expected to be an object annotated with {@link Collection}
 *
 * @field the field to be used as a reference. Default is "id".
 * @orderBy the order by clause to be used when fetching the reference. Default is "id DESC".
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Reference {
    /**
     * @return The field to be used as a reference. Default is "id".
     */
    String field() default "id";

    /**
     * @return The order by clause to be used when fetching the reference. Default is "id DESC".
     */
    String orderBy() default "id DESC";
}
