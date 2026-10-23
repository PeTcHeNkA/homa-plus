package plus.gen.utl;


public class Buffer<T> extends Element<T> {
    public synchronized void put(T obj){
        next = new Element<>(next, obj);
    }

    public T flush(){
        if(next==null)return null;
        T result = next.value;
        next = next.next;
        return result;
    }

    public boolean hasNext(){
        return next != null;
    }

    public void clear(){
        value = null;
        next = null;
    }
}

class Element<T>{
    protected T value;
    protected Element<T> next;

    public Element(){}

    public Element(Element<T> e, T t){
        value = t;
        next = e;
    }
}