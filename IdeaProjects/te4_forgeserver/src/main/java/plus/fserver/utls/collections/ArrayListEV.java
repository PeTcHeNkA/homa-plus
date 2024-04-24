package plus.fserver.utls.collections;

import java.util.Arrays;


public class ArrayListEV<T> extends ArrayListE6<T>{
    private final int exp;

    public ArrayListEV(int exp) {
        this.exp = exp;
    }


    @Override
    public Object[] newArrayAuto() {
        return Arrays.copyOf(data, data.length + exp);
    }
}