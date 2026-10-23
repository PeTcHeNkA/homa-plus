package plus.fserver.api.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {
    String target();

    String name();

    Class<?> type() default Object.class;

    String tName() default "java.lang.Object";
}
