package plus.util;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class AppPCLTask extends PCLTask{
    public AppPCLTask(Class<?> clazz) {
        super(clazz);
    }


    public Object inst() {
        try {
            Method result = clazz.getDeclaredMethod("main", String[].class);
            result.invoke(null, new Object[]{new String[]{}});
            return result;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
