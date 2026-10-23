package net.minecraftforge.common.chunkio;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import plus.fserver.server.chunk.io.TChunkExecutor;
import plus.fserver.server.chunk.io.WrappedChunkRunnable;


@Deprecated
public class ChunkIOExecutor {
    public static Chunk syncChunkLoad(World world, AnvilChunkLoader loader, ChunkProviderServer provider, int x, int z){
        return TChunkExecutor.syncChunkLoad((WorldServer) world, loader, provider, x, z);
    }


    public static void queueChunkLoad(World world, AnvilChunkLoader loader, ChunkProviderServer provider, int x, int z, Runnable runnable) {
        TChunkExecutor.queueChunkLoad((WorldServer) world, loader, provider, x, z, new WrappedChunkRunnable(runnable));
    }


    public static void dropQueuedChunkLoad(World world, int x, int z, Runnable runnable) {
        TChunkExecutor.dropQueuedChunkLoad((WorldServer) world, x, z, new WrappedChunkRunnable(runnable));
    }


    public static void adjustPoolSize(int players) {
        //TChunkExecutor.adjustPoolSize(players);
    }
}