package plus.lyn.utl;

import plus.tson.utl.Te4HashMap;
import plus.tson.utl.TupleMutable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class Statister<V> extends Te4HashMap<V, AtomicInteger> {

    public void feed(V obj){
        AtomicInteger num;
        if((num = get(obj)) == null) fput(obj, new AtomicInteger(1));
        else num.addAndGet(1);
    }


    public void feed(V obj, int amount){
        AtomicInteger num;
        if((num = get(obj)) == null) fput(obj, new AtomicInteger(amount));
        else num.addAndGet(amount);
    }


    public TupleMutable<V[], int[]> dump(){
        TupleMutable<V[], int[]> field;
        dump(field = new TupleMutable<>());
        return field;
    }


    public void dump(TupleMutable<V[], int[]> field){
        V[] objects;
        int[] weights;
        if(field.B.length == size()){
            objects = field.A;
            weights = field.B;
        } else {
            field.A = objects = arr(field.A, size());
            field.B = weights = new int[objects.length];
        }

        int cursor = 0;
        for (Map.Entry<V,AtomicInteger> entry:entrySet()){
            objects[cursor] = entry.getKey();
            weights[cursor] = entry.getValue().get();
            ++cursor;
        }
    }


    private V[] arr(V[] src, int size){
        return (V[]) java.lang.reflect.Array
                .newInstance(src.getClass().getComponentType(), size);
    }
}