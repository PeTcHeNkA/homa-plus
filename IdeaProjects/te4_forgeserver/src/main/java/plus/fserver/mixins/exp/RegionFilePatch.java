package plus.fserver.mixins.exp;

import net.minecraft.world.chunk.storage.ChunkBuffer;
import net.minecraft.world.chunk.storage.RegionFile;
import net.minecraftforge.fml.common.FMLLog;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import plus.fserver.server.zstd.MapCoder;
import javax.annotation.Nullable;
import java.io.*;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;


@Mixin(RegionFile.class)
public abstract class RegionFilePatch {
    @Shadow protected abstract boolean outOfBounds(int x, int z);
    @Shadow protected abstract int getOffset(int x, int z);
    @Shadow private RandomAccessFile dataFile;
    @Shadow private List<Boolean> sectorFree;
    @Shadow @Final private File fileName;


    /**
     * @author Te4hno
     * @reason ZSTD
     */
    @Nullable
    @Overwrite
    public synchronized DataInputStream getChunkDataInputStream(int x, int z) {
        if (this.outOfBounds(x, z)) {
            return null;
        } else {
            try {
                int i = this.getOffset(x, z);
                if (i == 0) {
                    return null;
                } else {
                    int j = i >> 8;
                    int k = i & 255;
                    if (k == 255) {
                        this.dataFile.seek(j * 4096L);
                        k = (this.dataFile.readInt() + 4) / 4096 + 1;
                    }
                    if (j + k > this.sectorFree.size()) {
                        return null;
                    } else {
                        this.dataFile.seek(j * 4096L);
                        int l = this.dataFile.readInt();
                        if (l > 4096 * k) {
                            FMLLog.log.warn("Invalid chunk: ({}, {}) Offset: {} Invalid Size: {}>{} {}", x, z, j, l, k * 4096, fileName);
                            return null;
                        } else if (l <= 0) {
                            FMLLog.log.warn("Invalid chunk: ({}, {}) Offset: {} Invalid Size: {} {}", x, z, j, l, fileName);
                            return null;
                        } else {
                            byte b0 = this.dataFile.readByte();
                            if (b0 == 3) {
                                byte[] abyte = new byte[l - 1];
                                this.dataFile.read(abyte);
                                return new DataInputStream(new BufferedInputStream(MapCoder.newIStream(new ByteArrayInputStream(abyte))));
                            }
                            if (b0 == 2) {
                                byte[] abyte = new byte[l - 1];
                                this.dataFile.read(abyte);
                                return new DataInputStream(new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(abyte))));
                            }
                            if (b0 == 1) {
                                byte[] abyte1 = new byte[l - 1];
                                this.dataFile.read(abyte1);
                                return new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(abyte1))));
                            }
                            return null;
                        }
                    }
                }
            }
            catch (IOException var9) {
                return null;
            }
        }
    }


    /**
     * @author Te4hno
     * @reason ZSTD
     */
    @Nullable
    @Overwrite
    public DataOutputStream getChunkDataOutputStream(int x, int z) {
        try {
            return this.outOfBounds(x, z) ? null : new DataOutputStream(new BufferedOutputStream(
                    MapCoder.newOStream(new ChunkBuffer((RegionFile)(Object)this, x, z))
            ));
        } catch (IOException e) {
            return null;
        }
    }


    /**
     * @author Te4hno
     * @reason ZSTD
     */
    @Overwrite
    private void write(int sectorNumber, byte[] data, int length) throws IOException {
        this.dataFile.seek(sectorNumber * 4096L);
        this.dataFile.writeInt(length + 1);
        this.dataFile.writeByte(MapCoder.disabled?2:3);
        this.dataFile.write(data, 0, length);
    }
}