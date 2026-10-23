package plus.sol.api;


public interface IVarProxy<U, T> {
    T getValue(U user, IEnvironment<U> env);
    default void setValue(U user, IEnvironment<U> env, T value){
        throw new UnsupportedOperationException();
    }
    class Constant<U, T> implements IVarProxy<U, T>{
        private final T val;
        public Constant(T val) {
            this.val = val;
        }
        @Override
        public T getValue(U user, IEnvironment<U> env) {
            return val;
        }
    }
}