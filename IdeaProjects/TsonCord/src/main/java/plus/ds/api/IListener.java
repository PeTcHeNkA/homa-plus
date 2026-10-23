package plus.ds.api;


public interface IListener<E> {
    boolean handle(E event);
    default int priority(){return 0;};
}
