package plus.space.server.world;

import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.SPacketSpawnGlobalEntity;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.*;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import plus.fserver.api.ElcaServerUtls;
import plus.fserver.api.ElcaWorldServer;
import java.io.File;


public class BaseWorld extends WorldServerMulti implements ElcaWorldServer {
    public BaseWorld(MinecraftServer server, ISaveHandler saveHandlerIn, int dimensionId,
                     WorldServer delegate, Profiler profilerIn) {

        super(server, saveHandlerIn, dimensionId, delegate, profilerIn);
    }


    private static ISaveHandler saveHandler(MinecraftServer server, String name){
        ISaveHandler savehandler = ElcaServerUtls.getSaveHandler(server);
        if(savehandler==null)savehandler = new AnvilSaveHandler(
                new File("dyn_worlds"),
                "d_"+name, false, server.getDataFixer()
        );
        return savehandler;
    }


    private static WorldInfo worldInfo(MinecraftServer server, String name, WorldType type){
        return new WorldInfo(new WorldSettings(
                server.worlds[0].getSeed(),
                server.getGameType(),
                server.canStructuresSpawn(),
                server.isHardcore(), type
            ), name
        );
    }


    @Override
    public Teleporter getDefaultTeleporter() {
        return super.getDefaultTeleporter();
    }


    @Override
    public boolean addWeatherEffect(Entity entityIn) {
        this.weatherEffects.add(entityIn);
        this.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, entityIn.posX, entityIn.posY, entityIn.posZ, 512.0D, this.provider.getDimension(), new SPacketSpawnGlobalEntity(entityIn));
        return true;
    }
}