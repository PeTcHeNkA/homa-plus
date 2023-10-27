package plus.te4net.core.api;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


@FunctionalInterface
public interface IEventHandler<T> {
    boolean handle(T event);

    default Class<T> getHandledClass(){
        Type[] obj0 = getClass().getGenericInterfaces();
        for (Type curt:obj0) {
            if(curt instanceof ParameterizedType) {
                ParameterizedType type = (ParameterizedType) curt;
                return (Class<T>) type.getActualTypeArguments()[0];
            }
        }
        throw new RuntimeException("Cant search event automatically");
    }
}