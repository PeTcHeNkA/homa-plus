package plus.fserver.server.mergeworld;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import plus.fserver.api.WorldConstructor;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class MergedWorldManager {
    private final MinecraftServer server;
    private final CopyOnWriteArrayList<WorldServer> allWorlds = new CopyOnWriteArrayList<>();
    private MergedPlayerChunkMap pcm;
    private MergedChunkProviderServer mcpr;
    private ScheduledFuture<?> pcmFuture;
    private final int[] id2Thread;
    private int nextId;

    public MergedWorldManager(MinecraftServer serv, int minId, int maxId, DimensionType type){
        this.server = serv;
        int[] id2Thread = this.id2Thread = new int[16];

        int shift = 0;
        for (int id = minId; id < maxId; id++){
            id2Thread[shift] = id;
            WorldConstructor.register(id,
                    (server, saveHandler, dimId, delegate, profiler) ->
                            newC64(server, saveHandler, dimId, delegate, profiler), type
            );
            ++shift;
        }
        if(shift != id2Thread.length)throw new IllegalArgumentException();
    }


    private synchronized int nextMId(){
        return ++nextId;
    }


    private void onUnloadNode(WorldServer world){
        allWorlds.remove(world);
        if(pcm.getWorldServer() == world){
            if(allWorlds.isEmpty()){
                pcm = null;
            } else {
                pcm = new MergedPlayerChunkMap(allWorlds.get(0));
            }
        }
    }


    private MergedPlayerChunkMap getOrCreateMPCM(WorldServer worldServer){
        if(pcm != null)return pcm;
        pcm = new MergedPlayerChunkMap(worldServer);

        pcmFuture = worldServer.getMinecraftServer().ticker().service.scheduleAtFixedRate(() -> {
            if (pcm != null) pcm.realTick();
            else {
                if(pcmFuture != null)pcmFuture.cancel(true);
                pcmFuture = null;
            }
        }, 25, 50, TimeUnit.MILLISECONDS);
        return pcm;
    }


    protected WorldServerC64 newC64(MinecraftServer server, ISaveHandler saveHandler, int dimId, WorldServer delegate, Profiler profiler){
        return new WorldServerC64(server, saveHandler, dimId, delegate, profiler) {
            private final int[] table = id2Thread;
            {
                allWorlds.add(this);
            }
            @Override
            protected int foundDimByPos(int cx, int cz) {
                return table[getThr(cx, cz, 3, 2, 3, 1)];
            }

            @Override
            public int nextEntityId() {
                return nextMId();
            }

            @Override
            protected void createTracker() {
                this.entityTracker = new MegredEntityTracker(this) {
                    @Override
                    protected Iterable<WorldServer> additionalSources() {
                        return allWorlds;
                    }
                }.init();
            }


            @Override
            protected PlayerChunkMap createPCM() {
                return getOrCreateMPCM(this);
            }


            @Override
            public void onUnload() {
                onUnloadNode(this);
            }
        };
    }


    private static int getThr(int x, int z, int rootSize, int tBit, int tMask, int shiftZ){
        return (((x >> rootSize) & tMask) << tBit) | (((z + shiftZ) >> (rootSize + tBit)) & tMask);
    }
}