package plus.fserver.world.addon;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.util.ITeleporter;
import plus.fserver.api.WorldConstructor;


public class EmptyConstructor implements WorldConstructor.Constructor {
    private static EmptyConstructor INSTANCE;

    public static EmptyConstructor get(){
        return INSTANCE == null ? INSTANCE = new EmptyConstructor() : INSTANCE;
    }


    @Override
    public WorldServerMulti create(MinecraftServer server, ISaveHandler saveHandler, int dimId, WorldServer delegate, Profiler profiler) {
        return new WorldServerMulti(server, saveHandler, dimId, delegate, profiler){
            @Override
            public IChunkProvider createChunkProvider() {
                return new MemoryChunkProviderServer(this);
            }
            @Override
            public IChunkGenerator createChunkGenerator() {
                return new EmptyChunkGenerator(this);
            }
            @Override
            public ITeleporter getDefaultTeleporterRaw() {
                return EmptyTeleporter.INSTANCE;
            }
            @Override
            public boolean canUnload() {
                return playerEntities.isEmpty() && _syncTasks.isEmpty() &&
                        !provider.getDimensionType().shouldLoadSpawn();
            }
        };
    }
}