package net.minecraft.world.chunk.storage;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import net.minecraftforge.fml.common.*;
import java.util.zip.*;
import org.jetbrains.annotations.Nullable;
import plus.fserver.server.zstd.MapCoder;
import java.io.*;


public class RegionFile {
    private static final boolean FORGE_ENABLE_EXTENDED_SAVE;
    private static final byte[] EMPTY_SECTOR_PART;
    private final File fileName;
    private RandomAccessFile dataFile;
    private final int[] offsets;
    private BooleanArrayList sectorFree;

    public RegionFile(final File fileNameIn) {
        this.offsets = new int[1024];
        this.fileName = fileNameIn;
        try {
            //if (fileNameIn.exists()) {
            //  this.lastModified = fileNameIn.lastModified();
            //}
            final RandomAccessFile dataFile = this.dataFile = new RandomAccessFile(fileNameIn, "rw");
            if (dataFile.length() < 4096L) {
                writeEmptySector(dataFile);
                writeEmptySector(dataFile);
                //this.sizeDelta += 8192;
            }
            if ((dataFile.length() & 0xFFFL) != 0x0L) {
                for (int i = 0; i < (dataFile.length() & 0xFFFL); ++i) {
                    dataFile.write(0);
                }
            }
            final int size = (int)dataFile.length() / 4096;
            final BooleanArrayList sectorFree = this.sectorFree = new BooleanArrayList(size);
            for (int j = 0; j < size; ++j) {
                sectorFree.add(true);
            }
            sectorFree.set(0, false);
            sectorFree.set(1, false);
            dataFile.seek(0L);
            for (int offsetIdx = 0; offsetIdx < 1024; ++offsetIdx) {
                final int offset = dataFile.readInt();
                setOffset(offsetIdx, offset);

                int length = offset & 0xFF;
                if (length == 255 && offset >> 8 <= sectorFree.size()) {
                    dataFile.seek((offset >> 8) * 4096L);
                    length = (dataFile.readInt() + 4) / 4096 + 1;
                    dataFile.seek(offsetIdx * 4 + 4);
                }
                if (offset != 0 && (offset >> 8) + length <= sectorFree.size()) {
                    for (int l = 0; l < length; ++l) {
                        sectorFree.set((offset >> 8) + l, false);
                    }
                }
                else if (length > 0) {
                    FMLLog.log.warn("Invalid chunk: ({}, {}) Offset: {} Length: {} runs off end file. {}", offsetIdx % 32, offsetIdx / 32, offset >> 8, length, fileNameIn);
                }
            }
            //todo maybe remove?
//            for (int k2 = 0; k2 < 1024; ++k2) {
//                //dataFile.
//                final int l2 = dataFile.readInt();
//                //this.chunkTimestamps[k2] = l2;
//            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Deprecated
    public synchronized boolean chunkExists(final int x, final int z) {
        return this.isChunkSaved(x, z);
    }


    //ZSTD
    @Nullable
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


    //ZSTD
    @Nullable
    public DataOutputStream getChunkDataOutputStream(int x, int z) {
        try {
            return this.outOfBounds(x, z) ? null : new DataOutputStream(new BufferedOutputStream(
                    MapCoder.newOStream(new ChunkBuffer(x, z))
            ));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    //ZSTD
    private void write(int sectorNumber, byte[] data, int length) throws IOException {
        RandomAccessFile dataFile = this.dataFile;
        dataFile.seek(sectorNumber * 4096L);
        dataFile.writeInt(length + 1);
        dataFile.writeByte(MapCoder.disabled?2:3);
        dataFile.write(data, 0, length);
    }


    protected synchronized void write(final int x, final int z, final byte[] data, final int length) {
        try {
            final int i = this.getOffset(x, z);
            int j = i >> 8;
            int k = i & 0xFF;
            if (k == 255) {
                this.dataFile.seek(j * 4096L);
                k = (this.dataFile.readInt() + 4) / 4096 + 1;
            }
            final int l = (length + 5) / 4096 + 1;
            if (l >= 256) {
                if (!RegionFile.FORGE_ENABLE_EXTENDED_SAVE) {
                    return;
                }
                FMLLog.log.warn("Large Chunk Detected: ({}, {}) Size: {} {}", x, z, l, this.fileName);
            }
            if (j != 0 && k == l) {
                this.write(j, data, length);
            } else {
                final BooleanArrayList sectorFree = this.sectorFree;
                for (int i2 = 0; i2 < k; ++i2) {
                    sectorFree.set(j + i2, true);
                }
                int l2 = sectorFree.indexOf(true);
                int j2 = 0;
                if (l2 != -1) {
                    for (int k2 = l2; k2 < sectorFree.size(); ++k2) {
                        if (j2 != 0) {
                            if (sectorFree.getBoolean(k2)) {
                                ++j2;
                            }
                            else {
                                j2 = 0;
                            }
                        } else if (sectorFree.getBoolean(k2)) {
                            l2 = k2;
                            j2 = 1;
                        }
                        if (j2 >= l) {
                            break;
                        }
                    }
                }
                if (j2 >= l) {
                    j = l2;
                    this.setOffset(x, z, l2 << 8 | (Math.min(l, 255)));
                    for (int j3 = 0; j3 < l; ++j3) {
                        sectorFree.set(j + j3, false);
                    }
                    this.write(j, data, length);
                } else {
                    this.dataFile.seek(this.dataFile.length());
                    j = this.sectorFree.size();
                    for (int i3 = 0; i3 < l; ++i3) {
                        writeEmptySector(dataFile);
                        sectorFree.add(false);
                    }
                    this.write(j, data, length);
                    this.setOffset(x, z, j << 8 | (Math.min(l, 255)));
                }
            }
        }
        catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }


    private boolean outOfBounds(final int x, final int z) {
        return x < 0 || x >= 32 || z < 0 || z >= 32;
    }


    private int getOffset(final int x, final int z) {
        return this.offsets[x + z * 32];
    }


    public boolean isChunkSaved(final int x, final int z) {
        return this.getOffset(x, z) != 0;
    }


    private void setOffset(int index, int offset){
        this.offsets[index] = offset;
    }


    private void setOffset(final int x, final int z, final int offset) throws IOException {
        setOffset(x + z * 32, offset);
        this.dataFile.seek((x + z * 32L) * 4);
        this.dataFile.writeInt(offset);
    }


    @Deprecated
    private void setChunkTimestamp(final int x, final int z, final int timestamp) throws IOException {}


    public void close() throws IOException {
        if (this.dataFile != null) {
            this.dataFile.close();
        }
    }


    static {
        FORGE_ENABLE_EXTENDED_SAVE = Boolean.parseBoolean(System.getProperty("forge.enableExtendedSave", "true"));
        EMPTY_SECTOR_PART = new byte[1024];//new byte[4096];
    }


    private static void writeEmptySector(RandomAccessFile raf) throws IOException {
        raf.write(EMPTY_SECTOR_PART, 0, 1024);
        raf.write(EMPTY_SECTOR_PART, 0, 1024);
        raf.write(EMPTY_SECTOR_PART, 0, 1024);
        raf.write(EMPTY_SECTOR_PART, 0, 1024);
    }


    class ChunkBuffer extends ByteArrayOutputStream {
        private final int chunkX;
        private final int chunkZ;

        public ChunkBuffer(final int x, final int z) {
            super(8096);
            this.chunkX = x;
            this.chunkZ = z;
        }

        @Override
        public void close() throws IOException {
            RegionFile.this.write(this.chunkX, this.chunkZ, this.buf, this.count);
        }
    }
}
