package plus.client.mem;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.client.entity.ISpMemory;


@SideOnly(Side.CLIENT)
public class Memory implements ISpMemory {
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


    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void setActive(boolean b) {

    }

    @Override
    public double getPrevXZRot() {
        return 0;
    }

    @Override
    public double getPrevYRot() {
        return 0;
    }

    @Override
    public void setPrevXZRot(double v) {

    }

    @Override
    public void setPrevYRot(double v) {

    }

    @Override
    public double getCurXZRot() {
        return 0;
    }

    @Override
    public double getCurYRot() {
        return 0;
    }

    @Override
    public void setCurRot(double xz, double y) {

    }

    @Override
    public void setCurXZRot(double xz) {

    }

    @Override
    public void setCurYRot(double y) {

    }

    @Override
    public int getFreezeTimes() {
        return 0;
    }

    @Override
    public void setFreezeTimes(int t) {

    }
}