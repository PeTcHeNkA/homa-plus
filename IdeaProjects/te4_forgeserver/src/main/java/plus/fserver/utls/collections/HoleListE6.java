package plus.fserver.utls.collections;


public class HoleListE6<T> extends ArrayListE6<T>{
    private final Object[] nonEmpty = new Object[1];
    @Override
    public Object[] newArrayAuto() {
        size = 0;
        return nonEmpty;
    }


    public HoleListE6<T> free(){
        size = 0;
        nonEmpty[0] = null;
        return this;
    }
}