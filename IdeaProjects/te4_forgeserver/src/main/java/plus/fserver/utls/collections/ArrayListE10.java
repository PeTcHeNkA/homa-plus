package plus.fserver.utls.collections;

import java.util.Arrays;


public class ArrayListE10<T> extends ArrayListE6<T>{
    @Override
    public Object[] newArrayAuto() {
        return Arrays.copyOf(data, data.length + 10);
    }
}