package plus.sol.utls.flux;


public interface Flux<T> {
    boolean hasNext();
    T next();
}
