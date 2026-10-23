package plus.utls;


public interface IConstructorModify<T extends IConstructorModify<T>> {
    default void onCallConstruct(){}
}