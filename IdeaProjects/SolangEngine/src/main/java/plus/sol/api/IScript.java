package plus.sol.api;


public interface IScript<U> {
    boolean run(U user);
    default int getID(){
        return -1;
    }
}