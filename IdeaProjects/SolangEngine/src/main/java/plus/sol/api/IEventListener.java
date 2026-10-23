package plus.sol.api;


public interface IEventListener<U, E> {
    boolean onCallEvent(U user, E arg);
}
