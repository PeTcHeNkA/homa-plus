package plus.fserver.utls;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;


public class Reflector {
    public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static <T> Constructor<T> getCtor(Class<T> clazz, Class<?>... args) {
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor(args);
            ctor.setAccessible(true);
            return ctor;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static MethodHandle resolveCtor(Class<?> clazz, Class<?>... args) {
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor(args);
            ctor.setAccessible(true);
            return LOOKUP.unreflectConstructor(ctor);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }
}