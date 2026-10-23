package plus.quest.memory;


public class MemorySector<T> implements AbstractMemorySector<T> {
    private T value;

    public MemorySector(){}

    public MemorySector(String data, MemoryEncoder<T> encoder){
        value = encoder.encode(data);
    }


    public MemorySector(T data){
        value = data;
    }


    @Override
    public void set(T value){
        this.value = value;
    }


    @Override
    public T getValue(){
        return value;
    }


    @Override
    public String toString(){
        return value.toString();
    }
}