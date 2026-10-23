package plus.fserver.world.addon;

import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.IChunkLoader;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;


public class EmptyChunkLoader implements IChunkLoader {
    private static EmptyChunkLoader INSTANCE;
    private EmptyChunkLoader(){}

    public static EmptyChunkLoader get(){
        if(INSTANCE == null){
            INSTANCE = new EmptyChunkLoader();
        }
        return INSTANCE;
    }

    @Nullable
    @Override
    public Chunk loadChunk(World worldIn, int x, int z) throws IOException {
        return null;
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
        return false;
    }
}