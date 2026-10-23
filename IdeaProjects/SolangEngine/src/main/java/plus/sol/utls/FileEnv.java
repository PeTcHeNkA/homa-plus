package plus.sol.utls;

import plus.sol.api.IEnvironment;


public interface FileEnv<U> extends IEnvironment<U>  {
    int getCursor();
    void setCursor(int cursor);
    char next();
    char current();
    boolean hasNext();

    default int length(){
        throw new UnsupportedOperationException();
    }

    default IEnvironment<U> getParent(){
        throw new UnsupportedOperationException();
    }


    default void forValue(CharConsumer chr){
        while (hasNext()){
            chr.accept(next());
        }
    }
}