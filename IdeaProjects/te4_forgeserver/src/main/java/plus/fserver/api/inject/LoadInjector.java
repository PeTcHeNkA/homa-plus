package plus.fserver.api.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE})
public @interface LoadInjector {
    //String path() default "";
    Field field() default @Field(target = "", name = "");
    Field[] fields() default {};
}