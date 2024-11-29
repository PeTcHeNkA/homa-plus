package plus.fserver.world.addon.pvpworld;

import net.minecraftforge.fml.common.FMLLog;
import plus.tson.utl.Te4HashMap;
import java.util.function.Consumer;


public class TaskRegistry extends Te4HashMap<String, Consumer<?>> {
    public static final Consumer<?>  EMPTY = (Consumer<Object>) o -> {};
    public static final TaskRegistry REGISTRY = new TaskRegistry();

    private TaskRegistry(){
        fput("empty", EMPTY);
        fput("null", EMPTY);
    }


    public <T> Consumer<T> getTask(String key){
        if(key == null || key.isEmpty())return empty();
        Object raw = super.get(key);
        if(raw == null)return empty();
        try {
            Consumer<T> task = (Consumer<T>) raw;
            return task;
        } catch (Exception e){
            FMLLog.log.error("Try use task ["+key+"], but error: "+e.getMessage());
            return empty();
        }
    }


    public static <T> Consumer<T> empty(){
        return (Consumer<T>) EMPTY;
    }


    public static boolean isEmpty(Consumer<?> task){
        return task == EMPTY;
    }


    public static <T> Consumer<T> and(Consumer<T> first, Consumer<T> second){
        if(first  == EMPTY)return second;
        if(second == EMPTY)return first;
        return new And<>(first, second);
    }


    private static class And<T> implements Consumer<T>{
        private final Consumer<T> first, second;

        private And(Consumer<T> first, Consumer<T> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public void accept(T o) {
            first.accept(o);
            second.accept(o);
        }
    }
}