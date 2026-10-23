package plus.utl.Tmp;


public class TemporalObject<Value>{

    private static int livingTime = 90;
    public static void setGlobalLivingTime(int time){
        livingTime = time;
    }
    public static int getGlobalLivingTime(){
        return livingTime;
    }

    protected final int maxTime;
    public final Value value;
    protected int time = 0;

    public TemporalObject(Value value){
        this.value = value;
        this.maxTime = livingTime;
        this.time = maxTime;
    }


    public TemporalObject(Value value, int livingTime){
        this.value = value;
        this.maxTime = livingTime;
        this.time = maxTime;
    }


    public void regenTime(){
        time = maxTime;
    }
}
