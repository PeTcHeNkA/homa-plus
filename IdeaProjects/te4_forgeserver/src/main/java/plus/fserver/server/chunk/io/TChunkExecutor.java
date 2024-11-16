package plus.fserver.server.chunk.io;

import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.FMLLog;
import java.util.function.Consumer;


public class TChunkExecutor {
    public static Chunk syncChunkLoad(WorldServer world, AnvilChunkLoader loader,
                                      ChunkProviderServer provider, int x, int z) {
        TQueuedChunk key = new TQueuedChunk(x, z);

        TChunkTask task = world.getChunkTasks().remove(key);

        if (task != null) {
            task.await();
        } else {
            task = new TChunkIOProvider(world, key, loader, provider);
            task.run();
        }
        task.syncCallback();
        return task.getChunk();
    }


    public static void queueChunkLoad(WorldServer world, AnvilChunkLoader loader, ChunkProviderServer provider,
                                      int x, int z, Consumer<Chunk> runnable) {
        TQueuedChunk key = new TQueuedChunk(x, z);

        TChunkTask task = world.getChunkTasks().get(key);
        if (task == null) {
            task = new TChunkIOProvider(world, key, loader, provider);
            task.addCallback(runnable);
            world.addChunkTask(key, task);
            world.getMinecraftServer().ticker().service.execute(task);
        } else {
            task.addCallback(runnable);
        }
    }


    public static boolean queueChunkUnload(WorldServer world, AnvilChunkLoader loader, ChunkProviderServer provider, Chunk chunk) {
        if(chunk.isNowSaving())
            return false;
        TQueuedChunk key = new TQueuedChunk(chunk.x, chunk.z);

        TChunkTask task = world.getChunkTasks().get(key);
        if (task == null) {
            chunk.setSaveStatus(true);
            chunk.onUnload();
            if(!chunk.needsSaving(true)){
                TChunkUnloadTask.onUnload(chunk);
                return true;
            }
            task = new TChunkUnloadTask(world, key, loader, provider, chunk);
            world.addChunkTask(key, task);
            world.getMinecraftServer().ticker().service.execute(task);
            return true;
        } else {
            return false;
        }
    }


    public static Chunk syncChunkGen(WorldServer world, AnvilChunkLoader loader,
                                      ChunkProviderServer provider, int x, int z) {
        TQueuedChunk key = new TQueuedChunk(x, z);

        TChunkTask task = world.getChunkTasks().remove(key);

        if (task != null) {
            task.await();
        } else {
            task = new TChunkGenProvider(world, key, loader, provider);
            task.run();
        }
        task.syncCallback();
        return task.getChunk();
    }


    public static void queueChunkGenerate(WorldServer world, AnvilChunkLoader loader, ChunkProviderServer provider,
                                          int x, int z, Consumer<Chunk> runnable) {
        TQueuedChunk key = new TQueuedChunk(x, z);

        TChunkTask task = world.getChunkTasks().get(key);
        if (task == null) {
            task = new TChunkGenProvider(world, key, loader, provider);
            task.addCallback(runnable);
            world.addChunkTask(key, task);
            world.getMinecraftServer().ticker().service.execute(task);
        } else {
            task.addCallback(runnable);
        }
    }


    public static void dropQueuedChunkLoad(WorldServer world, int x, int z, Consumer<Chunk> runnable) {
        TQueuedChunk key = new TQueuedChunk(x, z);

        TChunkTask task = world.getChunkTasks().get(key);

        if (task == null) {
            FMLLog.log.warn("Attempted to dequeue chunk that wasn't queued? {} @ ({}, {})",
                    world.provider.getDimension(), x, z);
            return;
        }
        task.removeCallback(runnable);
        if (!task.hasCallback()) {
            if(task.cansel()) world.getChunkTasks().remove(key);
        }
    }
}