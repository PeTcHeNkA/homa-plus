package plus.quest.memory;

public interface AbstractMemorySector<T> {
    void set(T value);

    T getValue();
}
