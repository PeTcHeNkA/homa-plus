package plus.fserver.world.addon;

import net.minecraft.entity.EMoveHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.ISaveHandler;
import plus.fserver.api.WorldConstructor;


public class MemoryWorld extends WorldServerMulti {
    public MemoryWorld(MinecraftServer server, ISaveHandler handler, int id, WorldServer parent, Profiler profiler) {
        super(server, handler, id, parent, profiler);
    }


    @Override
    protected void initUnimportant() {
        collideTask = new EMoveHelper.ComboCollideTask(16);
    }


    @Override
    public IChunkGenerator createChunkGenerator() {
        return new EmptyChunkGenerator(this);
    }


    public static WorldConstructor.Constructor makeCtor(NBTTagCompound dump) {
        return new WorldConstructor.Constructor() {
            @Override
            public WorldServerMulti create(MinecraftServer server, ISaveHandler saveHandler, int dimId, WorldServer delegate, Profiler profiler) {
                return new MemoryWorld(server, saveHandler, dimId, delegate, profiler) {
                    @Override
                    public IChunkProvider createChunkProvider() {
                        return new MemoryChunkProviderServer(this, new DumpChunkLoader(dump, false), createChunkGenerator());
                    }
                };
            }
        };
    }
}