package plus.fserver.api;

import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.IChunkLoader;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;


public interface IAsyncChunkLoader extends IChunkLoader {
    @Nullable
    Object[] loadAsync(WorldServer world, int x, int z) throws IOException;
}