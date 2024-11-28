package plus.fserver.world.addon;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import plus.fserver.utls.NbtUtils;


public class MemoryChunkProviderServer extends ChunkProviderServer {
    public MemoryChunkProviderServer(WorldServer world){
        this(world, EmptyChunkLoader.get(), new EmptyChunkGenerator(world));
    }


    public MemoryChunkProviderServer(WorldServer world, IChunkLoader chunkLoader, IChunkGenerator chunkGeneratorIn) {
        super(world, chunkLoader, chunkGeneratorIn);
    }


    @Override
    public boolean canSave() {
        return false;
    }


    public static NBTTagCompound dumpToNbt(ChunkProviderServer cps, int minX, int minZ, int maxX, int maxZ){
        NBTTagCompound nbt = new NBTTagCompound();
        for (int cx = minX; cx < maxX; cx++){
            for (int cz = minZ; cz < maxZ; cz++){
                if(!cps.isChunkGeneratedAt(cx, cz))continue;
                Chunk chunk = cps.provideChunk(cz, cz);
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
        return "chunk_"+x+"_"+z;
    }
}