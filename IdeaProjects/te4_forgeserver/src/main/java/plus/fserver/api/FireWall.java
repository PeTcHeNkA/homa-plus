package plus.fserver.api;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import java.net.InetAddress;
import java.util.ArrayList;
import static plus.fserver.ElcaConfig.DEFAULT_SOFT_TIME;


public interface FireWall {
    default void softSetAll(MinecraftServer server, long time, boolean allow){
        ArrayList<EntityPlayerMP> list = new ArrayList<>(server.getPlayerList().getPlayers());
        for(EntityPlayerMP player : list){
            softSet(player, time, allow);
        }
    }


    default boolean softSet(EntityPlayerMP player, boolean allow){
        return softSet(player, DEFAULT_SOFT_TIME, allow);
    }


    boolean softSet(EntityPlayerMP player, long time, boolean allow);


    void softSet(InetAddress address, long time, boolean allow);


    default void softSet(InetAddress address, boolean allow){
        softSet(address, DEFAULT_SOFT_TIME, allow);
    }
}