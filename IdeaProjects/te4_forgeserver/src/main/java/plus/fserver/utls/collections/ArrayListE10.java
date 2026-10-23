package plus.fserver.utls.collections;


public class ArrayListE10<T> extends ArrayListE6<T>{
    @Override
    public Object[] newArrayAuto() {
        Object[] data;
        return expand(data = this.data, data.length+10);
    }
}