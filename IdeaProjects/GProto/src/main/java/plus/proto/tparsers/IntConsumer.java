package plus.proto.tparsers;

public interface IntConsumer {
    void apply(int id);
    default void apply(long id){
        apply((int)id);
    }
}