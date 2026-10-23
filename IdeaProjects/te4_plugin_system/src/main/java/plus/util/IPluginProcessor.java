package plus.util;


public interface IPluginProcessor<C> {
    void onLoad(C obj);
    default void onComplete(){}
    Class<?> getSupportedType();
}
