package plus.fserver.api.inject;

import java.lang.annotation.Annotation;
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


    class Container implements Field{
        private final String target, name, tName;
        private final Class<?> type;

        public Container(String target, String name, Class<?> type, String tName) {
            if(target == null)target = "";
            if(name == null)name = "";
            if(type == null)type = Object.class;
            if(tName == null)tName = "java.lang.Object";
            this.target = target;
            this.name = name;
            this.tName = tName;
            this.type = type;
        }

        @Override
        public String target() {
            return target;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Class<?> type() {
            return type;
        }

        @Override
        public String tName() {
            return tName;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Field.class;
        }
    }
}
