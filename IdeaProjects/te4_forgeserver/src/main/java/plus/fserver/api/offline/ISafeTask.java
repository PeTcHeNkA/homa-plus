package plus.fserver.api.offline;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import plus.fserver.api.ElcaServerUtils;
import java.util.UUID;
import java.util.function.Consumer;


public interface ISafeTask extends Consumer<EntityPlayerMP> {
    default TaskType type(){
        return TaskType.SYNC;
    }


    default boolean advancements(){
        return true;
    }


    default void run(MinecraftServer server, UUID uuid){
        ElcaServerUtils.applyForPlayerSafe(server, uuid, this, type(), advancements());
    }
}