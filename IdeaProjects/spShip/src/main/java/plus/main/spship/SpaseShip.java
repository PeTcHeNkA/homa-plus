package plus.main.spship;


import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.EntitySkeleton;
import net.minecraft.world.level.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;


//import org.bukkit.entity.Player;


public class SpaseShip{
    public static void test(String world0, double x, double y, double z){
        WorldServer world = ((CraftWorld) Bukkit.getWorld(world0)).getHandle();

        EEntitySkeleton ams = new EEntitySkeleton(world,x,y,z);
    }
}

