package plus.fserver.world.save;

import org.jetbrains.annotations.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;


public interface IMapData {
    @Nullable
    DataInputStream  getChunkInputStream (final File dir, final int chunkX, final int chunkZ);
    DataOutputStream getChunkOutputStream(final File dir, final int chunkX, final int chunkZ);
    boolean chunkExists(File dir, int x, int z);
    default void clearCache(){};
}