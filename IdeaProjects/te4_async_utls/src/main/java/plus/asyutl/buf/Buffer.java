package plus.asyutl.buf;


public class Buffer<T> implements IBuffer<T>{
    private Element<T> next;
    @Override
    public void put(T obj){
        synchronized (this) {
            next = new Element<>(next, obj);
        }
    }

    @Override
    public boolean flush(Field<T> target){
        synchronized (this) {
            if (next == null){
                target.value = null;
                return false;
            }
            target.value = next.value;
            next = next.next;
            return true;
        }
    }


    @Override
    public boolean hasNext(){
        synchronized (this) {
            return next != null;
        }
    }


    @Override
    public void clear(){
        next = null;
    }


    protected static final class Element<T>{
        final T value;
        final Element<T> next;
        Element(Element<T> e, T t){
            value = t;
            next = e;
        }
    }
}