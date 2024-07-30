package plus.fserver.utls.collections;

import java.util.Collection;


public class ArrayRemSwapListE6<T> extends ArrayListE6<T> {
    public ArrayRemSwapListE6(){}

    public ArrayRemSwapListE6(Object[] data, int len){
        this.data = data;
        this.size = len;
    }


    public ArrayRemSwapListE6(Object... data){
        this.data = data;
        this.size = data.length;
    }


    @Override
    public T remove(int index) {
        if(size == index+1){
            T prev = (T) data[--size];
            data[size] = null;
            return prev;
        }
        T prev = (T) data[index];
        Object swap = data[size-1];
        data[index] = swap;
        data[size-1] = null;
        --size;
        return prev;
    }


    @Override
    public boolean removeAll(Collection<?> c) {
        Object[] data = this.data;
        int size, prevSize = size = this.size;
        Object swap;
        for (int i = 0; i < size; i++){
            if(c.contains(data[i])){
                if (i != --size) {
                    swap = data[size];
                    data[i--] = swap;
                }
                data[size] = null;
            }
        }
        this.size = size;
        return size != prevSize;
    }


    public void removeSwap(int index) {
        if(size == index+1){
            data[--size] = null;
            return;
        }
        Object swap = data[size-1];
        data[index] = swap;
        data[size-1] = null;
        --size;
    }


    @Override
    public boolean contains(Object o) {
        for (int i = 0, s = size; i < s; i++) if(o.equals(data[i]))return true;
        return false;
    }


    @Override
    public boolean remove(Object o) {
        for (int index = 0, s = size; index < s; index++)
            if (o.equals(data[index])) {
                remove(index);
                return true;
            }
        return false;
    }


    public boolean removeRefSwap(Object o) {
        for (int index = 0, s = size; index < s; index++)
            if (o == data[index]) {
                removeSwap(index);
                return true;
            }
        return false;
    }


    @Override
    public boolean add(T obj) {
        if(obj == null){return false;}
        return super.add(obj);
    }


    public boolean removeNotNulDef(Object o) {
        for (int index = 0; index < size; index++)
            if (o.equals(data[index])) {
                int numMoved = size - index - 1;
                if (numMoved > 0) System.arraycopy(data, index + 1, data, index, numMoved);
                data[--size] = null;
                return true;
            }
        return false;
    }
}
