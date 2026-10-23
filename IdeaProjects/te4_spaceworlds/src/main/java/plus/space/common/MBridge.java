package plus.space.common;


public class MBridge {
    public static BFunc<GalName> CAN_FLY_TO = obj -> true;


    @FunctionalInterface
    public interface BFunc<T>{
        boolean isSucces(T obj);
    }
}