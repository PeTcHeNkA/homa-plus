package plus.fserver.server.chunk.io;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.FMLLog;

import java.util.function.Consumer;


public class TChunkExecutor {
    public static Chunk syncChunkLoad(WorldServer world, AnvilChunkLoader loader,
                                      IChunkProvider provider, int x, int z) {
        TQueuedChunk key = new TQueuedChunk(x, z, world);

        TChunkTask task = world.getChunkTasks().remove(key);

        if (task != null) {
            task.await();
        } else {
            task = new TChunkIOProvider(key, loader, (ChunkProviderServer) provider);
            task.run();
        }
        task.syncCallback();
        return task.getChunk();
    }


    public static void queueChunkLoad(WorldServer world, AnvilChunkLoader loader, IChunkProvider provider,
                                      int x, int z, Consumer<Chunk> runnable) {
        TQueuedChunk key = new TQueuedChunk(x, z, world);

        TChunkTask task = world.getChunkTasks().get(key);
        if (task == null) {
            task = new TChunkIOProvider(key, loader, (ChunkProviderServer) provider);
            task.addCallback(runnable);
            world.getChunkTasks().put(key, task);
            world.getMinecraftServer().ticker().service.execute(task);
        } else {
            task.addCallback(runnable);
        }
    }


    public static void queueChunkUnload(WorldServer world, AnvilChunkLoader loader, IChunkProvider provider, Chunk chunk) {
        TQueuedChunk key = new TQueuedChunk(chunk.x, chunk.z, world);

        TChunkTask task = world.getChunkTasks().get(key);
        if (task == null) {
            task = new TChunkUnloadTask(key, loader, (ChunkProviderServer) provider, chunk);
            world.getChunkTasks().put(key, task);
            world.getMinecraftServer().ticker().service.execute(task);
        }
    }


    public static Chunk syncChunkGen(World world, AnvilChunkLoader loader,
                                      IChunkProvider provider, int x, int z) {
        TQueuedChunk key = new TQueuedChunk(x, z, world);

        WorldServer sworld = (WorldServer) world;
        TChunkTask task = sworld.getChunkTasks().remove(key);

        if (task != null) {
            task.await();
        } else {
            task = new TChunkGenProvider(key, loader, (ChunkProviderServer) provider);
            task.run();
        }
        task.syncCallback();
        return task.getChunk();
    }


    public static void queueChunkGenerate(WorldServer world, AnvilChunkLoader loader, IChunkProvider provider,
                                          int x, int z, Consumer<Chunk> runnable) {
        TQueuedChunk key = new TQueuedChunk(x, z, world);

        TChunkTask task = world.getChunkTasks().get(key);
        if (task == null) {
            task = new TChunkGenProvider(key, loader, (ChunkProviderServer) provider);
            task.addCallback(runnable);
            world.getChunkTasks().put(key, task);
            world.getMinecraftServer().ticker().service.execute(task);
        } else {
            task.addCallback(runnable);
        }
    }


    public static void dropQueuedChunkLoad(World world, int x, int z, Consumer<Chunk> runnable) {
        TQueuedChunk key = new TQueuedChunk(x, z, world);

        WorldServer sworld = (WorldServer) world;
        TChunkTask task = sworld.getChunkTasks().get(key);

        if (task == null) {
            FMLLog.log.warn("Attempted to dequeue chunk that wasn't queued? {} @ ({}, {})",
                    world.provider.getDimension(), x, z);
            return;
        }
        task.removeCallback(runnable);
        if (!task.hasCallback()) {
            task.cansel();
            sworld.getChunkTasks().remove(key);
        }
    }


    public static void adjustPoolSize(int players) {
        //pool.setCorePoolSize(Math.max(BASE_THREADS, players / PLAYERS_PER_THREAD));
    }
}