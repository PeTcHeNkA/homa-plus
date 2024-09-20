package asm;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;


public class TestAsm2 {
    World world;


    public static int vi1(Entity entity){
        if(entity.world.isRemote){
            return 1;
        } else {
            return 2;
        }
    }


    public static int vi2(Entity entity){
        if(!entity.world.isRemote){
            return 1;
        } else {
            return 2;
        }
    }


    public int vi3(){
        if(!world.isRemote){
            return 1;
        } else {
            return 2;
        }
    }
}
