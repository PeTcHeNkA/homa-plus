package plus.bw.utl.collect;

import java.util.Comparator;


public class Int2ObjectTuple<T> {
    public static final Comparator<Int2ObjectTuple<?>> NUM_COMPARATOR = (o1, o2) -> {
        int x = o1.num, y = o2.num;
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    };

    public final int num;
    public final T obj;

    public Int2ObjectTuple(int num, T obj) {
        this.num = num;
        this.obj = obj;
    }
}
