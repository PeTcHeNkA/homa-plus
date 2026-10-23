package plus.mobs.utl;

import java.util.ArrayList;
import java.util.function.Consumer;


public class Delegate<T> extends ArrayList<Consumer<T>> implements Consumer<T>{
    @Override
    public void accept(T arg) {
        for(Consumer<T> func:this){
            func.accept(arg);
        }
    }
}