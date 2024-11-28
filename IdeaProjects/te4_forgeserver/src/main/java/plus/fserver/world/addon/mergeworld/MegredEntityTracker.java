package plus.fserver.world.addon.mergeworld;

import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;
import java.util.ArrayList;
import java.util.List;

public abstract class MegredEntityTracker extends EntityTracker {
    protected final ArrayList<EntityPlayer> allPlayers = new ArrayList<>();

    public MegredEntityTracker(WorldServer world) {
        super(world);
    }

    protected abstract Iterable<WorldServer> additionalSources();


    @Override
    public void tick() {
        ArrayList<EntityPlayer> allPlayers = this.allPlayers;
        allPlayers.clear();
        allPlayers.addAll(world.curPlayerEntities);

        for (WorldServer worldServer:additionalSources()){
            allPlayers.addAll(worldServer.curPlayerEntities);
        }
        super.tick();
    }


    @Override
    protected List<EntityPlayer> trackedPlayers() {
        return allPlayers;
    }
}