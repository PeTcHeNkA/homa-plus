package plus.asyutl.buf;

public interface IBuffer<T> {
    boolean flush(Field<T> target);
    void put(T obj);
    boolean hasNext();
    void clear();

    class Field<T>{
        public T value;
    }
}