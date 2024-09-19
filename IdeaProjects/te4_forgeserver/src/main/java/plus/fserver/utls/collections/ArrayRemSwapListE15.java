package plus.fserver.utls.collections;


public class ArrayRemSwapListE15<T> extends ArrayRemSwapListE6<T>{
    @Override
    public Object[] newArrayAuto() {
        Object[] data;
        return expand(data = this.data, data.length + 15);
    }
}