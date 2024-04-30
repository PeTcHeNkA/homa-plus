package plus.fserver.utls.collections;

import java.util.Arrays;


public final class ArrayRemSwapListE1V<T> extends ArrayRemSwapListE6<T> {
    public ArrayRemSwapListE1V(T v) {
        this.data = new Object[]{v};
        this.size = 1;
    }

    @Override
    public Object[] newArrayAuto() {
        return Arrays.copyOf(data, data.length == 1 ? 5 : data.length + 7);
    }
}
