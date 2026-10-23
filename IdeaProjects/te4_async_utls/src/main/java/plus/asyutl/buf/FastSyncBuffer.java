package plus.asyutl.buf;


public class FastSyncBuffer<T> extends FastBuffer<T> {
    @Override
    public void put(T obj) {
        synchronized (this){
            super.put(obj);
        }
    }


    @Override
    public T flush() {
        synchronized (this){
            return super.flush();
        }
    }


    @Override
    public T get() {
        synchronized (this) {
            return super.get();
        }
    }
}
