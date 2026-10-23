package plus.proto.tparsers;


public interface IntConsumer {
    int index();


    void setIndex(int index);


    String gefGroup();


    void apply(String group, int id);
}