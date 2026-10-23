package plus.fserver.world.generator.struct;


public class StructPower{
    private int power;

    public StructPower(int max){
        power = max;
    }


    public boolean use(){
        return power--<1;
    }


    public boolean stop(){
        return power<1;
    }
}