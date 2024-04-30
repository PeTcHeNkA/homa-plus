package plus.fserver.utls.collections;

import java.util.Arrays;


public class ArrayRemSwapListE15<T> extends ArrayRemSwapListE6<T>{
    @Override
    public Object[] newArrayAuto() {
        return Arrays.copyOf(data, data.length + 15);
    }
}