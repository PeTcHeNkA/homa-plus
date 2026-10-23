package plus.asyutl.buf;

public class FastBuffer<T> {
    private Buffer.Element<T> next;

    public void put(T obj){
        next = new Buffer.Element<>(next, obj);
    }


    public T flush(){
        if(next==null)return null;
        T result = next.value;
        next = next.next;
        return result;
    }


    public T get(){
        return next==null?null: next.value;
    }


    public T get(int n){
        if(next==null)return null;
        Buffer.Element<T> next = this.next;
        for(int i=0;i<n;i++){
            next = next.next;
        }
        return next==null?null: next.value;
    }


    public boolean hasNext(){
        return next != null;
    }


    public void clear(){
        next = null;
    }
}