package net.minecraft.world.chunk.storage;

import java.io.*;
import net.minecraft.util.datafix.*;
import net.minecraft.world.*;
import net.minecraft.nbt.*;
import javax.annotation.*;
import net.minecraft.world.storage.*;


public class AnvilSaveHandler extends SaveHandler {
    public AnvilSaveHandler(final File p_i46650_1_, final String saveDirectoryName, final boolean p_i46650_3_, final DataFixer dataFixerIn) {
        super(p_i46650_1_, saveDirectoryName, p_i46650_3_, dataFixerIn);
    }


    @Override
    public IChunkLoader getChunkLoader(final WorldProvider provider) {
        final File file1 = this.getWorldDirectory();
        if (provider.getSaveFolder() != null) {
            final File file2 = new File(file1, provider.getSaveFolder());
            file2.mkdirs();
            return new AnvilChunkLoader(file2, this.dataFixer);
        }
        return new AnvilChunkLoader(file1, this.dataFixer);
    }


    @Override
    public void saveWorldInfoWithPlayer(final WorldInfo worldInformation, @Nullable final NBTTagCompound tagCompound) {
        worldInformation.setSaveVersion(19133);
        super.saveWorldInfoWithPlayer(worldInformation, tagCompound);
    }


    @Override
    public void flush() {
//        try {
//            ThreadedFileIOBase.getThreadedIOInstance().waitForFinish();
//        }
//        catch (InterruptedException interruptedexception) {
//            interruptedexception.printStackTrace();
//        }
//        RegionFileCache.clearRegionFileReferences();
    }
}
