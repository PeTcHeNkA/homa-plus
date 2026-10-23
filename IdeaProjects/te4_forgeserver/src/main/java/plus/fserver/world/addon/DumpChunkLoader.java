package plus.fserver.world.addon;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;
import org.jetbrains.annotations.Nullable;
import plus.fserver.api.IAsyncChunkLoader;
import plus.fserver.server.SStrings;
import plus.fserver.server.zstd.MapCoder;
import plus.fserver.utls.EmptyChunk;
import plus.fserver.utls.NbtUtils;
import plus.fserver.world.light.LightingEngine;
import plus.tson.utl.Tuple;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;


public class DumpChunkLoader implements IAsyncChunkLoader {
    private static final Tuple<Chunk,NBTTagCompound> EMPTY = new Tuple<>(EmptyChunk.INSTANCE, new NBTTagCompound(2,1));
    private final NBTTagCompound data;
    private final boolean useBiomeRef;

    public DumpChunkLoader(NBTTagCompound data, boolean useBiomeRef) {
        this.data = data;
        this.useBiomeRef = useBiomeRef;
    }


    @Nullable
    @Override
    public Object[] loadAsync(WorldServer world, int x, int z) throws IOException {
        Tuple<Chunk, NBTTagCompound> data = createChunk(this.data, world, x, z, useBiomeRef);
        return new Object[]{data.A, data.B};
    }


    @Nullable
    @Override
    public Chunk loadChunk(World worldIn, int x, int z) throws IOException {
        Tuple<Chunk, NBTTagCompound> data = createChunk(this.data, worldIn, x, z, useBiomeRef);
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


    public static NBTTagCompound dumpToNbt(ChunkProviderServer cps, int minX, int minZ, int maxX, int maxZ){
        LightingEngine le = cps.world.getLightingEngine();
        le.awaitUnlock();
        le.processLightUpdates();
        le.awaitUnlock();
        NBTTagCompound nbt = new NBTTagCompound();
        for (int cx = minX; cx < maxX; cx++){
            for (int cz = minZ; cz < maxZ; cz++){
                if(!cps.isChunkGeneratedAt(cx, cz))continue;
                Chunk chunk = cps.provideChunk(cx, cz);
                if(isEmptyBlocks(chunk))continue;

                NBTTagCompound chunkData = new NBTTagCompound();
                NbtUtils.writeChunkToNBT(null, chunk, cps.world, chunkData, false);
                nbt.setTag(chunkKey(cx, cz), chunkData);
            }
        }
        return nbt;
    }


    public static boolean isEmptyBlocks(Chunk chunk){
        for (ExtendedBlockStorage storage:chunk.getBlockStorageArray()){
            if(storage != null && !storage.isEmpty())return false;
        }
        if(!chunk.getTileEntityMap().isEmpty())return false;
        return true;
    }


    public static String chunkKey(int x, int z){
        return "С_"+x+"_"+z;
    }


    public static Tuple<Chunk,NBTTagCompound> createChunk(NBTTagCompound data, World world, int x, int z, boolean useBiomeRef){
        String key = chunkKey(x, z);
        if(data.hasKey(key)){
            NBTTagCompound nbt = data.getCompoundTag(key);
            return new Tuple<>(createFromNbt(nbt, world, useBiomeRef), nbt);
        }
        return new Tuple<>(EmptyChunkGenerator.newEmptyChunk(world, x, z), EMPTY.B);
    }


    private static Chunk createFromNbt(NBTTagCompound nbt, World world, boolean useBiomeRef){
        return NbtUtils.readChunkFromNBT(world, nbt, useBiomeRef);
    }


    public static void saveDumpTo(NBTTagCompound dump, String path, Runnable callBack){
        try {
            MapCoder.writeCompressed(dump, new File(path));
            if(callBack != null) callBack.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void readDumpFrom(String path, Consumer<NBTTagCompound> callBack){
        if(callBack == null)return;
        try {
            NBTTagCompound nbt = MapCoder.readCompressed(new File(path));
            callBack.accept(nbt);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}