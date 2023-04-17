package plus.client.mem;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class Memory {
    public double prevXZrot = 0f;
    public float prevYrot = 0f;
    public boolean isActive = false;
    public int freezeTimes = 0;
    public int flag = 0;


    public void setPrevXZ(double xz){
        prevXZrot = xz;
    }
    public void setPrevY(float y){
        prevYrot = y;
    }


    public Memory copy(){
        Memory mem = new Memory();
        mem.prevYrot = prevYrot;
        mem.prevXZrot = prevXZrot;
        mem.isActive = isActive;
        mem.freezeTimes = freezeTimes;
        mem.flag = flag;
        return mem;
    }
}