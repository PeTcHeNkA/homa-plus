package plus.fserver.api;

import net.minecraft.world.gen.IChunkGenerator;


public interface ElcaWorldServer {
    default IChunkGenerator createChunkGenerator(){throw new UnsupportedOperationException();}
    default void addSyncTask(Runnable r){}
    default void addAsyncTask(Runnable r){}
    default boolean canUnload(){return false;}
}