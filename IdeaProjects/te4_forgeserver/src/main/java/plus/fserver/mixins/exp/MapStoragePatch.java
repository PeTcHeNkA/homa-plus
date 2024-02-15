package plus.fserver.mixins.exp;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import javax.annotation.Nullable;
import java.io.*;
import java.util.List;
import java.util.Map;
import static plus.fserver.server.zstd.MapCoder.newIStream;
import static plus.fserver.server.zstd.MapCoder.newOStream;


@Mixin(MapStorage.class)
public class MapStoragePatch {
    @Shadow @Final private ISaveHandler saveHandler;
    @Shadow protected Map<String, WorldSavedData> loadedDataMap;
    @Shadow @Final private List<WorldSavedData> loadedDataList;

    /**
     * @author Te4hno
     * @reason ZSTD compressing
     */
    @Nullable
    @Overwrite
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
                        }
                        catch (Exception exception) {
                            throw new RuntimeException("Failed to instantiate " + clazz, exception);
                        }

                        NBTTagCompound nbttagcompound = null;
                        FileInputStream fileinputstream = null;
                        try {
                            fileinputstream = new FileInputStream(file1);
                            nbttagcompound = readCompressed0(fileinputstream);
                            fileinputstream.close();
                        } catch (Exception e){
                            if(fileinputstream != null) fileinputstream.close();
                            FMLLog.log(Level.ERROR, "Exception ZSTD encode ["+dataIdentifier+"], try use GZIP");

                            fileinputstream = new FileInputStream(file1);
                            nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);

                            fileinputstream.close();
                        }

                        worldsaveddata.readFromNBT(nbttagcompound.getCompoundTag("data"));
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


    /**
     * @author Te4hno
     * @reason ZSTD compressing
     */
    @Overwrite
    private void saveData(WorldSavedData data) {
        if (this.saveHandler != null) {
            try {
                File file1 = this.saveHandler.getMapFileFromName(data.mapName);

                if (file1 != null) {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    nbttagcompound.setTag("data", data.writeToNBT(new NBTTagCompound()));
                    FileOutputStream fileoutputstream = new FileOutputStream(file1);

                    writeCompressed0(nbttagcompound, fileoutputstream);

                    fileoutputstream.close();
                }
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }


    private void writeCompressed0(NBTTagCompound compound, OutputStream stream) throws IOException {
        try (DataOutputStream dataoutputstream = new DataOutputStream(new BufferedOutputStream(
                newOStream(stream)
        ))) {
            CompressedStreamTools.write(compound, dataoutputstream);
        }
    }


    private NBTTagCompound readCompressed0(InputStream stream) throws IOException {
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