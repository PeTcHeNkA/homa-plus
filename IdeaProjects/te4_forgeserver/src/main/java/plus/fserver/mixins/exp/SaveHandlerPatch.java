package plus.fserver.mixins.exp;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.event.ForgeEventFactory;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


@Mixin(SaveHandler.class)
public class SaveHandlerPatch {
    @Shadow @Final private File playersDirectory;
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final protected DataFixer dataFixer;

    /**
     * @author Te4hno
     * @reason ZSTD
     */
    @Overwrite
    public void writePlayerData(EntityPlayer player) {
        try {
            NBTTagCompound nbttagcompound = player.writeToNBT(new NBTTagCompound());
            File file1 = new File(this.playersDirectory, player.getCachedUniqueIdString() + ".dat.tmp");
            File file2 = new File(this.playersDirectory, player.getCachedUniqueIdString() + ".dat");
            CompressedStreamTools.writeCompressed(nbttagcompound, new FileOutputStream(file1));

            if (file2.exists()) {
                file2.delete();
            }

            file1.renameTo(file2);
            net.minecraftforge.event.ForgeEventFactory.firePlayerSavingEvent(player, this.playersDirectory, player.getUniqueID().toString());
        }
        catch (Exception var5) {
            LOGGER.warn("Failed to save player data for {}", player.getName());
        }
    }


    /**
     * @author Te4hno
     * @reason ZSTD
     */
    @Overwrite
    public NBTTagCompound readPlayerData(EntityPlayer player) {
        NBTTagCompound nbttagcompound = null;
        try {
            File file1 = new File(this.playersDirectory, player.getCachedUniqueIdString() + ".dat");
            if (file1.exists() && file1.isFile()) {
                nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file1));
            }
        }
        catch (Exception var4) {
            LOGGER.warn("Failed to load player data for {}", player.getName());
        }

        if (nbttagcompound != null) {
            player.readFromNBT(this.dataFixer.process(FixTypes.PLAYER, nbttagcompound));
        }
        ForgeEventFactory.firePlayerLoadingEvent(player, playersDirectory, player.getUniqueID().toString());
        return nbttagcompound;
    }
}