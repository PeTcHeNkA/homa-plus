package plus.fserver.utls.collections;

import java.util.Arrays;


public class ArrayListE2<T> extends ArrayListE6<T>{
    @Override
    public Object[] newArrayAuto() {
        return Arrays.copyOf(data, data == EMPTY_ARR ?1:data.length+2);
    }
}