package plus.main.spship;

import net.minecraft.world.phys.Vec3D;


public abstract class AutoPilot {

    public boolean isActive = false;
    protected Vec3D currentStartPoint = null;
    protected int time = 0;


    public void active(Vec3D vec, int time){
        currentStartPoint = vec;
        isActive = true;
        this.time = time;
    }


    public void disable(){
        isActive = false;
    }


    public abstract Vec3D next(int time);


    public Vec3D next(){
        if(--time<0){
            isActive = false;
            time = 0;
        }
        return next(time);
    }
}