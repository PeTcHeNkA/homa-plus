package plus.fserver.utls.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;


public class ArraySkipList<T> implements Iterable<T>{
    private final int expa;
    private Object[] data;
    int cursor = 0;
    int size = 0;

    public ArraySkipList(int expa) {
        if(expa<=0)throw new IllegalArgumentException("Expa should be >= 1");
        this.expa = expa;
        data = new Object[expa];
    }


    public int append(T obj){
        int cursor = this.cursor;
        if(cursor>=data.length){
            Object[] newArr = new Object[data.length+expa];
            System.arraycopy(data, 0, newArr, 0, data.length);
            data = newArr;
            data[cursor] = obj;
            this.cursor += 1;
            size = this.cursor;
            return cursor;
        } else {
            if(data[cursor] == null){
                data[cursor] = obj;
                this.cursor = cursor+1;
                if(size<this.cursor)size = this.cursor;
                return cursor;
            } else {
                for (cursor=0;cursor<data.length;++cursor){
                    if(data[cursor] == null){
                        data[cursor] = obj;
                        this.cursor = cursor+1;
                        return cursor;
                    }
                }
                this.cursor = data.length;
                Object[] newArr = new Object[data.length+expa];
                System.arraycopy(data, 0, newArr, 0, data.length);
                data = newArr;
                data[this.cursor] = obj;
                size = this.cursor+1;
                return this.cursor;
            }
        }
    }


    public void trim(){
        Object[] data = new Object[size];
        int size = 0;
        for (Object obj:this){
            data[size] = obj;
            ++size;
        }
        if(size + expa > data.length){
            Object[] newArr = new Object[size + expa];
            System.arraycopy(data, 0, newArr, 0, size);
            this.data = newArr;
        } else {
            this.data = data;
        }
    }


    public void remove(int index){
        if(index>=data.length)return;
        data[index] = null;
        cursor = index;
    }


    public int remove(T obj){
        Object[] data = this.data;
        for (int i=0;i<data.length;i++){
            if(Objects.equals(data[i], obj)){
                this.data[i] = null;
                return this.cursor = i;
            }
        }
        return -1;
    }


    public void set(int index, T obj){
        if(index>=data.length){
            int range = index-data.length;
            Object[] newArr = new Object[data.length+range+expa];
            System.arraycopy(data, 0, newArr, 0, data.length);
            data = newArr;
            size = index+1;
        }
        data[index] = obj;
        if(obj==null)cursor = index;
    }


    public T get(int index){
        if(index>=data.length)return null;
        return (T) data[index];
    }


    @Override
    public Iterator<T> iterator(){
        return new Iter<>(data);
    }


    private static final class Iter<T> implements Iterator<T>{
        private final Object[] data;
        private int cursor = 0;
        private Object next;

        public Iter(Object[] data) {
            this.data = data;
            next();
        }


        @Override
        public boolean hasNext() {
            return next != null;
        }


        @Override
        public T next() {
            Object result = next;
            while (++cursor<data.length) if((next = data[cursor]) != null)break;
            return (T) result;
        }
    }


    @Override
    public String toString() {
        Object[] data = this.data;
        int size = this.size;
        StringBuilder builder = new StringBuilder("[");
        Object temp;
        for (int i=0;i<size;i++)
            if((temp = data[i]) != null) builder.append(temp).append(',');

        builder.setCharAt(builder.length()-1, ']');
        return builder.toString();
    }


    public void clear(){
        Object[] data = this.data;
        int size = this.size;
        for (int i=0;i<size;i++){
            data[i] = null;
        }
        cursor = this.size = 0;
    }


    public int size(){
        return size;
    }
}

