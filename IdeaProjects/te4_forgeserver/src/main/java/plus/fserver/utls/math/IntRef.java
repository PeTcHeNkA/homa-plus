package plus.fserver.utls.math;


public class IntRef {
    private int value;

    public IntRef(){}
    public IntRef(int value){this.value = value;}

    public int get(){
        return value;
    }

    public void set(int value){
        this.value = value;
    }

    public void add(int value){
        this.value += value;
    }
}