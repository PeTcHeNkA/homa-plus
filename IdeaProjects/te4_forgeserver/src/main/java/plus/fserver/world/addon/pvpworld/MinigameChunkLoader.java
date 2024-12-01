package plus.fserver.world.addon.pvpworld;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.IChunkLoader;
import org.jetbrains.annotations.Nullable;
import plus.fserver.server.SStrings;
import plus.fserver.utls.NbtUtils;
import plus.tson.utl.Tuple;
import java.io.IOException;


public class MinigameChunkLoader implements IChunkLoader {
    private final MinigameController arena;

    public MinigameChunkLoader(MinigameController arena) {
        this.arena = arena;
    }


    @Nullable
    @Override
    public Chunk loadChunk(World worldIn, int x, int z) throws IOException {
        Tuple<Chunk, NBTTagCompound> data = arena.createChunk(worldIn, x, z);
        NbtUtils.loadEntities(worldIn, data.B.getCompoundTag(SStrings.CL_LEVEL), data.A);
        return data.A;
    }


    @Override
    public void saveChunk(World worldIn, Chunk chunkIn) throws MinecraftException, IOException {}


    @Override
    public void saveExtraChunkData(World worldIn, Chunk chunkIn) throws IOException {}


    @Override
    public void chunkTick() {}


    @Override
    public void flush() {}


    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        return true;
    }
}