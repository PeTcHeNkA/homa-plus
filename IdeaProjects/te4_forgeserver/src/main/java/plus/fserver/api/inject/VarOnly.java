package plus.fserver.api.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface VarOnly {
    //check ElcaConfig.getVar(var)
    String var();

    //default value. If result == true, marked element removed
    boolean def() default false;


    //ir result == true, remove only code
    boolean code() default false;
}