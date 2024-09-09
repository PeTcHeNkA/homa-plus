package plus.fserver.api.inject;

public interface FieldAccessor<I,T> {
    T get(I inst);
    void set(I inst, T t);
}
