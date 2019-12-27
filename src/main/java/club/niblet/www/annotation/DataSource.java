package club.niblet.www.annotation;

import java.lang.annotation.*;

/**
 * DataSource Annotation
 * @author niblet
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DataSource {
    String value() default "default";
}
