package asm;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import plus.fserver.api.inject.VarOnly;


public class TestAsm2 {

    @VarOnly(var = "SIDE_SERVER", def = true, code = true)
    public static void vi1(Entity entity){
        if(entity.world.isRemote){
            System.out.println(1);
        } else {
            System.out.println(2);
        }
    }
}
