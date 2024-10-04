package plus.fserver.server.mergeworld;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.world.WorldServer;


public class MergedPlayerChunkMap extends PlayerChunkMap {
    private MegredEntityTracker chunkMap;

    public MergedPlayerChunkMap(WorldServer serverWorld) {
        super(serverWorld);
        chunkMap = (MegredEntityTracker) serverWorld.getEntityTracker();
    }


    @Override
    public void tick() {
        //realTick();
    }


    public void realTick(){
        super.tick();
        for (EntityPlayer player:chunkMap.trackedPlayers()){
            EntityPlayerMP mp = (EntityPlayerMP) player;
            if(mp.shouldPCMUpdate()){
                super.updateMovingPlayer(mp);
                mp.setShouldPCMUpdate(false);
            }
        }
    }


    @Override
    public void updateMovingPlayer(EntityPlayerMP player) {
        player.setShouldPCMUpdate(true);
    }
}
