package plus.fserver.world.save;

import net.minecraft.world.chunk.storage.RegionFile;
import net.minecraft.world.chunk.storage.RegionFileCache;
import plus.fserver.ElcaConfig;
import plus.fserver.server.zstd.MapCoder;
import java.io.*;
import java.util.HashMap;


public class ERMapData implements IMapData{
    private final HashMap<File, ErChunkFile> cache = new HashMap<>();
    private final int maxOpens;

    public ERMapData(int maxOpens) {
        this.maxOpens = maxOpens;
    }


    @Override
    public synchronized DataInputStream getChunkInputStream(final File dir, final int x, final int z){
        File subDir     = new File(dir, "region");
        checkDir(subDir);

        try {
            ErChunkFile erf = get(subDir, x, z, false);
            if(erf == null)return null;
            return erf.readC(x, z);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean needConvert(){
        return ElcaConfig.ENABLE_ER2_CONVERTER;
    }


    @Override
    public synchronized DataOutputStream getChunkOutputStream(File dir, int x, int z) {
        File subDir = new File(dir, "region");
        checkDir(subDir);
        try {
            ErChunkFile erf = get(subDir, x, z, true);
            if(erf == null)return null;
            return erf.writeC(x, z);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public boolean chunkExists(File dir, int x, int z) {
        File subDir = new File(dir, "region");
        checkDir(subDir);

        try {
            ErChunkFile erf = get(subDir, x, z, false);
            if(erf == null)return false;
            return erf.contains(x, z);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    private synchronized ErChunkFile get(File subDir, int x, int z, boolean create) throws IOException {
        File file = new File(subDir, fileName(x, z));
        ErChunkFile erf = cache.get(file);
        if(erf != null)return erf;

        if(!file.exists()){
            if(needConvert()){
                File mca = RegionFileCache.getFile(subDir, x, z);
                if (mca.exists()) {
                    erf = new ErChunkFile(file);
                    convert(new RegionFile(mca), erf);
                    mca.delete();
                }
            }
            else if(!create) return null;
        }

        if(erf == null) {
            erf = new ErChunkFile(file);

            if(needConvert()){
                File mca = RegionFileCache.getFile(subDir, x, z);
                if (mca.exists()) {
                    convert(new RegionFile(mca), erf);
                    mca.delete();
                }
            }
            cache.put(file, erf);
            clearIfNeed();
        }
        return erf;
    }


    private static void convert(RegionFile from, ERMapData.ErChunkFile to) throws IOException {
        try {
            MCAtoER2Converter.MCAToER2(from, to);
        } catch (Exception e) {
            throw new IOException("Critical error on convert MCA (" + from + ") to ER2 (" + to + ")", e);
        }
    }


    private synchronized void clearIfNeed() throws IOException {
        HashMap<File, ErChunkFile> ch = this.cache;
        if(ch.size() > maxOpens){
            final long[] last = {Long.MIN_VALUE};
            final File[] toRem = new File[1];
            ch.forEach((file, cFile) -> {
                long cur = cFile.lastUsage;
                if(cur > last[0]){
                    last[0] = cur;
                    toRem[0] = file;
                }
            });
            ErChunkFile val = ch.remove(toRem[0]);
            if(val != null)val.close();
        }
    }


    private static void checkDir(File dir){
        if(!dir.exists())dir.mkdirs();
        else if(dir.isFile()){
            throw new RuntimeException("File "+dir+" not directory!");
        }
    }


    protected static class ErChunkFile extends ERegionFile{
        long lastUsage = System.currentTimeMillis();
        ErChunkFile(File file) throws IOException {
            super(new RandomAccessFile(file, "rw"));
        }
        boolean contains(int x, int z){
            return getPos(x, z) != -1;
        }
        DataInputStream readC(int x, int z) throws IOException {
            lastUsage = System.currentTimeMillis();
            byte[] bytes = super.read(x, z);
            return new DataInputStream(new BufferedInputStream(MapCoder.newIStream(new ByteArrayInputStream(bytes))));
        }

        DataOutputStream writeC(int x, int z) throws IOException {
            lastUsage = System.currentTimeMillis();
            return new DataOutputStream(new BufferedOutputStream(
                    MapCoder.newOStream(new Buf(x, z))
            ));
        }
        class Buf extends ByteArrayOutputStream{
            final int x, z;
            Buf(int x, int z) {
                this.x = x;
                this.z = z;
            }
            @Override
            public void close() throws IOException {
                ErChunkFile.this.write(x, z, buf, count);
            }
        }
    }


    private static String fileName(final int x, final int z){
        return "r." + (x >> 5) + "." + (z >> 5) + ".er";
    }
}