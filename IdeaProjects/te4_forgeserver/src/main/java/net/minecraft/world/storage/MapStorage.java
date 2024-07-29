package net.minecraft.world.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.*;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import net.minecraft.nbt.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;

import static plus.fserver.server.zstd.MapCoder.newIStream;
import static plus.fserver.server.zstd.MapCoder.newOStream;


public class MapStorage {
    private final ISaveHandler saveHandler;
    protected Map<String, WorldSavedData> loadedDataMap = Maps.newHashMap();
    private final List<WorldSavedData> loadedDataList = Lists.newArrayList();
    private final Map<String, Short> idCounts = Maps.newHashMap();

    public MapStorage(ISaveHandler saveHandlerIn) {
        this.saveHandler = saveHandlerIn;
        this.loadIdCounts();
    }


    @Nullable
    public WorldSavedData getOrLoadData(Class <? extends WorldSavedData > clazz, String dataIdentifier) {
        WorldSavedData worldsaveddata = this.loadedDataMap.get(dataIdentifier);

        if (worldsaveddata != null) {
            return worldsaveddata;
        } else {
            if (this.saveHandler != null) {
                try {
                    File file1 = this.saveHandler.getMapFileFromName(dataIdentifier);

                    if (file1 != null && file1.exists()) {
                        try {
                            worldsaveddata = clazz.getConstructor(String.class).newInstance(dataIdentifier);
                        } catch (Exception exception) {
                            throw new RuntimeException("Failed to instantiate " + clazz, exception);
                        }

                        NBTTagCompound nbt = readTagFromFile(file1);
                        if(nbt != null){
                            worldsaveddata.readFromNBT(nbt.getCompoundTag("data"));
                        }
                    }
                } catch (Exception exception1) {
                    exception1.printStackTrace();
                }
            }

            if (worldsaveddata != null) {
                this.loadedDataMap.put(dataIdentifier, worldsaveddata);
                this.loadedDataList.add(worldsaveddata);
            }
            return worldsaveddata;
        }
    }


    public void setData(String dataIdentifier, WorldSavedData data) {
        if (this.loadedDataMap.containsKey(dataIdentifier)) {
            this.loadedDataList.remove(this.loadedDataMap.remove(dataIdentifier));
        }
        this.loadedDataMap.put(dataIdentifier, data);
        this.loadedDataList.add(data);
    }


    public void saveAllData() {
        saveAllData(null);
    }


    public void saveAllData(@Nullable World world) {
        if(saveHandler == null)return;
        for (int i = 0; i < this.loadedDataList.size(); ++i) {
            WorldSavedData data = this.loadedDataList.get(i);

            if (data.isDirty()) {
                File file = this.saveHandler.getMapFileFromName(data.mapName);
                if(data != null){

                    if(world == null){
                        this.saveData(file, data);
                        data.setDirty(false);
                    } else {
                        world.getMinecraftServer().ticker().runAsync(new SaveTask(file, data));
                    }
                }
            }
        }
    }


    private static class SaveTask implements Runnable{
        private final File file;
        private final NBTTagCompound data;

        private SaveTask(File file, WorldSavedData data) {
            this.file = file;
            NBTTagCompound nbt = data.writeToNBT(new NBTTagCompound());
            if(data.shouldCopyNbtOnAsync()){
                nbt = nbt.copy();
            }
            this.data = nbt;
            data.markDirty();
        }


        @Override
        public void run() {
            FileOutputStream stream = null;
            try {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setTag("data", data);
                stream = new FileOutputStream(file);

                writeCompressedZstd(nbttagcompound, stream);

                stream.close();
            } catch (Exception exception) {
                exception.printStackTrace();
                if(stream != null) try {
                    stream.close();
                } catch (IOException ignored) {}
            }
        }
    }


    private void saveData(File file1, WorldSavedData data) {
        try {
            if (file1 != null) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setTag("data", data.writeToNBT(new NBTTagCompound()));
                FileOutputStream fileoutputstream = new FileOutputStream(file1);
                CompressedStreamTools.writeCompressed(nbttagcompound, fileoutputstream);
                fileoutputstream.close();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    private void loadIdCounts() {
        try {
            this.idCounts.clear();

            if (this.saveHandler == null) {
                return;
            }

            File file1 = this.saveHandler.getMapFileFromName("idcounts");

            if (file1 != null && file1.exists()) {
                DataInputStream datainputstream = new DataInputStream(new FileInputStream(file1));
                NBTTagCompound nbttagcompound = CompressedStreamTools.read(datainputstream);
                datainputstream.close();

                for (String s : nbttagcompound.getKeySet()) {
                    NBTBase nbtbase = nbttagcompound.getTag(s);

                    if (nbtbase instanceof NBTTagShort) {
                        NBTTagShort nbttagshort = (NBTTagShort)nbtbase;
                        short short1 = nbttagshort.getShort();
                        this.idCounts.put(s, Short.valueOf(short1));
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    public int getUniqueDataId(String key) {
        Short oshort = this.idCounts.get(key);

        if (oshort == null) {
            oshort = 0;
        } else {
            oshort = (short)(oshort.shortValue() + 1);
        }

        this.idCounts.put(key, oshort);

        if (this.saveHandler == null) {
            return oshort.shortValue();
        } else {
            try {
                File file1 = this.saveHandler.getMapFileFromName("idcounts");

                if (file1 != null) {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();

                    for (String s : this.idCounts.keySet()) {
                        nbttagcompound.setShort(s, ((Short)this.idCounts.get(s)).shortValue());
                    }

                    DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file1));
                    CompressedStreamTools.write(nbttagcompound, dataoutputstream);
                    dataoutputstream.close();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return oshort.shortValue();
        }
    }


    private NBTTagCompound readTagFromFile(File file) throws IOException {
        NBTTagCompound tag = null;
        FileInputStream fileinputstream = null;
        try {
            fileinputstream = new FileInputStream(file);
            tag = readCompressedZstd(fileinputstream);
            fileinputstream.close();
        } catch (Exception e){
            if(fileinputstream != null) fileinputstream.close();
            FMLLog.log(Level.ERROR, "Exception ZSTD encode ["+file.getAbsolutePath()+"], try use GZIP");

            fileinputstream = new FileInputStream(file);
            tag = CompressedStreamTools.readCompressed(fileinputstream);

            fileinputstream.close();
        }
        return tag;
    }


    private static void writeCompressedZstd(NBTTagCompound compound, OutputStream stream) throws IOException {
        try (DataOutputStream dataoutputstream = new DataOutputStream(new BufferedOutputStream(
                newOStream(stream)
        ))) {
            CompressedStreamTools.write(compound, dataoutputstream);
        }
    }


    private static NBTTagCompound readCompressedZstd(InputStream stream) throws IOException {
        DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(
                newIStream(stream)
        ));
        NBTTagCompound nbttagcompound;
        try {
            nbttagcompound = CompressedStreamTools.read(datainputstream, NBTSizeTracker.INFINITE);
        } finally {
            datainputstream.close();
        }
        return nbttagcompound;
    }
}