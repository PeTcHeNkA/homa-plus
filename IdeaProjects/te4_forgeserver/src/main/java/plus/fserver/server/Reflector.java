package plus.fserver.server;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.util.function.Supplier;
import static plus.tson.utl.uns.UnsafeUtils.*;


public class Reflector {
    public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final long reflectCache = offset(Class.class, "reflectionData");
    private static final long cachedCtor   = offset(Class.class, "cachedConstructor");

    public static <T> Constructor<T> getCtor(Class<T> clazz, Class<?>... args) {
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor(args);
            ctor.setAccessible(true);
            cleanReflectCache(clazz);
            return ctor;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void cleanReflectCache(Class<?> clazz){
        set(clazz, reflectCache, null);
        set(clazz, cachedCtor  , null);
    }


    public static <T> Supplier<T> compileCtor(Class<T> clazz) {
        try {
            CallSite callSite = LambdaMetafactory.metafactory(
                    Reflector.LOOKUP,
                    "get",
                    MethodType.methodType(Supplier.class),
                    MethodType.methodType(Object.class),
                    resolveCtor(clazz),
                    MethodType.genericMethodType(0)
            );
            return (Supplier<T>) callSite.getTarget().invokeExact();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } catch (LambdaConversionException e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public static MethodHandle resolveCtor(Class<?> clazz, Class<?>... args) {
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor(args);
            ctor.setAccessible(true);
            cleanReflectCache(clazz);
            return LOOKUP.unreflectConstructor(ctor);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }
}