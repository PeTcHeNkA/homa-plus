import asm.Test;

public class TestASM3 {
    private static boolean TEST = true;
    private Test.World world;

    boolean isRemote1(){
        return this.world.isRemote;
    }
    boolean isRemote2(){
        return !this.world.isRemote;
    }

    void test1(){
        if(TEST){
            System.out.println("!");
        }
    }
}
