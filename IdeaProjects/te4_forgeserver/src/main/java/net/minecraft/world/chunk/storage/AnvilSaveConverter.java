package net.minecraft.world.chunk.storage;

import com.google.common.collect.Lists;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.init.Biomes;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveFormatOld;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSummary;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.world.generator.biome.BiomeProviderMc;


@Deprecated
public class AnvilSaveConverter extends SaveFormatOld {
    private static final Logger LOGGER = LogManager.getLogger();

    public AnvilSaveConverter(File p_i46649_1_, DataFixer p_i46649_2_) {
        super(p_i46649_1_, p_i46649_2_);
    }

    @SideOnly(Side.CLIENT)
    public String getName() {
        return "Anvil";
    }

    @SideOnly(Side.CLIENT)
    public List<WorldSummary> getSaveList() throws AnvilConverterException {
        if (this.savesDirectory != null && this.savesDirectory.exists() && this.savesDirectory.isDirectory()) {
            List<WorldSummary> lvt_1_1_ = Lists.newArrayList();
            File[] lvt_2_1_ = this.savesDirectory.listFiles();
            File[] var3 = lvt_2_1_;
            int var4 = lvt_2_1_.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                File lvt_6_1_ = var3[var5];
                if (lvt_6_1_.isDirectory()) {
                    String lvt_7_1_ = lvt_6_1_.getName();
                    WorldInfo lvt_8_1_ = this.getWorldInfo(lvt_7_1_);
                    if (lvt_8_1_ != null && (lvt_8_1_.getSaveVersion() == 19132 || lvt_8_1_.getSaveVersion() == 19133)) {
                        boolean lvt_9_1_ = lvt_8_1_.getSaveVersion() != this.getSaveVersion();
                        String lvt_10_1_ = lvt_8_1_.getWorldName();
                        if (StringUtils.isEmpty(lvt_10_1_)) {
                            lvt_10_1_ = lvt_7_1_;
                        }

                        long lvt_11_1_ = 0L;
                        lvt_1_1_.add(new WorldSummary(lvt_8_1_, lvt_7_1_, lvt_10_1_, 0L, lvt_9_1_));
                    }
                }
            }

            return lvt_1_1_;
        } else {
            throw new AnvilConverterException(I18n.translateToLocal("selectWorld.load_folder_access"));
        }
    }

    protected int getSaveVersion() {
        return 19133;
    }

    @SideOnly(Side.CLIENT)
    public void flushCache() {
        AnvilChunkLoader.getMapData().clearCache();
    }

    public ISaveHandler getSaveLoader(String p_75804_1_, boolean p_75804_2_) {
        return new AnvilSaveHandler(this.savesDirectory, p_75804_1_, p_75804_2_, this.dataFixer);
    }

    @SideOnly(Side.CLIENT)
    public boolean isConvertible(String p_154334_1_) {
        WorldInfo lvt_2_1_ = this.getWorldInfo(p_154334_1_);
        return lvt_2_1_ != null && lvt_2_1_.getSaveVersion() == 19132;
    }

    public boolean isOldMapFormat(String p_75801_1_) {
        WorldInfo lvt_2_1_ = this.getWorldInfo(p_75801_1_);
        return lvt_2_1_ != null && lvt_2_1_.getSaveVersion() != this.getSaveVersion();
    }

    public boolean convertMapFormat(String p_75805_1_, IProgressUpdate p_75805_2_) {
        p_75805_2_.setLoadingProgress(0);
        List<File> lvt_3_1_ = Lists.newArrayList();
        List<File> lvt_4_1_ = Lists.newArrayList();
        List<File> lvt_5_1_ = Lists.newArrayList();
        File lvt_6_1_ = new File(this.savesDirectory, p_75805_1_);
        File lvt_7_1_ = new File(lvt_6_1_, "DIM-1");
        File lvt_8_1_ = new File(lvt_6_1_, "DIM1");
        LOGGER.info("Scanning folders...");
        this.addRegionFilesToCollection(lvt_6_1_, lvt_3_1_);
        if (lvt_7_1_.exists()) {
            this.addRegionFilesToCollection(lvt_7_1_, lvt_4_1_);
        }

        if (lvt_8_1_.exists()) {
            this.addRegionFilesToCollection(lvt_8_1_, lvt_5_1_);
        }

        int lvt_9_1_ = lvt_3_1_.size() + lvt_4_1_.size() + lvt_5_1_.size();
        LOGGER.info("Total conversion count is {}", lvt_9_1_);
        WorldInfo lvt_10_1_ = this.getWorldInfo(p_75805_1_);
        Object lvt_11_2_;
        if (lvt_10_1_ != null && lvt_10_1_.getTerrainType() == WorldType.FLAT) {
            lvt_11_2_ = new BiomeProviderSingle(Biomes.PLAINS);
        } else {
            lvt_11_2_ = new BiomeProviderMc(lvt_10_1_);
        }

        this.convertFile(new File(lvt_6_1_, "region"), lvt_3_1_, (BiomeProvider)lvt_11_2_, 0, lvt_9_1_, p_75805_2_);
        this.convertFile(new File(lvt_7_1_, "region"), lvt_4_1_, new BiomeProviderSingle(Biomes.HELL), lvt_3_1_.size(), lvt_9_1_, p_75805_2_);
        this.convertFile(new File(lvt_8_1_, "region"), lvt_5_1_, new BiomeProviderSingle(Biomes.SKY), lvt_3_1_.size() + lvt_4_1_.size(), lvt_9_1_, p_75805_2_);
        lvt_10_1_.setSaveVersion(19133);
        if (lvt_10_1_.getTerrainType() == WorldType.DEFAULT_1_1) {
            lvt_10_1_.setTerrainType(WorldType.DEFAULT);
        }

        this.createFile(p_75805_1_);
        ISaveHandler lvt_12_1_ = this.getSaveLoader(p_75805_1_, false);
        lvt_12_1_.saveWorldInfo(lvt_10_1_);
        return true;
    }

    private void createFile(String p_75809_1_) {
        File lvt_2_1_ = new File(this.savesDirectory, p_75809_1_);
        if (!lvt_2_1_.exists()) {
            LOGGER.warn("Unable to create level.dat_mcr backup");
        } else {
            File lvt_3_1_ = new File(lvt_2_1_, "level.dat");
            if (!lvt_3_1_.exists()) {
                LOGGER.warn("Unable to create level.dat_mcr backup");
            } else {
                File lvt_4_1_ = new File(lvt_2_1_, "level.dat_mcr");
                if (!lvt_3_1_.renameTo(lvt_4_1_)) {
                    LOGGER.warn("Unable to create level.dat_mcr backup");
                }

            }
        }
    }

    private void convertFile(File p_75813_1_, Iterable<File> p_75813_2_, BiomeProvider p_75813_3_, int p_75813_4_, int p_75813_5_, IProgressUpdate p_75813_6_) {
        Iterator var7 = p_75813_2_.iterator();

        while(var7.hasNext()) {
            File lvt_8_1_ = (File)var7.next();
            this.convertChunks(p_75813_1_, lvt_8_1_, p_75813_3_, p_75813_4_, p_75813_5_, p_75813_6_);
            ++p_75813_4_;
            int lvt_9_1_ = (int)Math.round(100.0 * (double)p_75813_4_ / (double)p_75813_5_);
            p_75813_6_.setLoadingProgress(lvt_9_1_);
        }

    }

    private void convertChunks(File p_75811_1_, File p_75811_2_, BiomeProvider p_75811_3_, int p_75811_4_, int p_75811_5_, IProgressUpdate p_75811_6_) {
        try {
            String lvt_7_1_ = p_75811_2_.getName();
            RegionFile lvt_8_1_ = new RegionFile(p_75811_2_);
            RegionFile lvt_9_1_ = new RegionFile(new File(p_75811_1_, lvt_7_1_.substring(0, lvt_7_1_.length() - ".mcr".length()) + ".mca"));

            for(int lvt_10_1_ = 0; lvt_10_1_ < 32; ++lvt_10_1_) {
                int lvt_11_1_;
                for(lvt_11_1_ = 0; lvt_11_1_ < 32; ++lvt_11_1_) {
                    if (lvt_8_1_.isChunkSaved(lvt_10_1_, lvt_11_1_) && !lvt_9_1_.isChunkSaved(lvt_10_1_, lvt_11_1_)) {
                        DataInputStream lvt_12_1_ = lvt_8_1_.getChunkDataInputStream(lvt_10_1_, lvt_11_1_);
                        if (lvt_12_1_ == null) {
                            LOGGER.warn("Failed to fetch input stream");
                        } else {
                            NBTTagCompound lvt_13_1_ = CompressedStreamTools.read(lvt_12_1_);
                            lvt_12_1_.close();
                            NBTTagCompound lvt_14_1_ = lvt_13_1_.getCompoundTag("Level");
                            ChunkLoader.AnvilConverterData lvt_15_1_ = ChunkLoader.load(lvt_14_1_);
                            NBTTagCompound lvt_16_1_ = new NBTTagCompound();
                            NBTTagCompound lvt_17_1_ = new NBTTagCompound();
                            lvt_16_1_.setTag("Level", lvt_17_1_);
                            ChunkLoader.convertToAnvilFormat(lvt_15_1_, lvt_17_1_, p_75811_3_);
                            DataOutputStream lvt_18_1_ = lvt_9_1_.getChunkDataOutputStream(lvt_10_1_, lvt_11_1_);
                            CompressedStreamTools.write(lvt_16_1_, lvt_18_1_);
                            lvt_18_1_.close();
                        }
                    }
                }

                lvt_11_1_ = (int)Math.round(100.0 * (double)(p_75811_4_ * 1024) / (double)(p_75811_5_ * 1024));
                int lvt_12_2_ = (int)Math.round(100.0 * (double)((lvt_10_1_ + 1) * 32 + p_75811_4_ * 1024) / (double)(p_75811_5_ * 1024));
                if (lvt_12_2_ > lvt_11_1_) {
                    p_75811_6_.setLoadingProgress(lvt_12_2_);
                }
            }

            lvt_8_1_.close();
            lvt_9_1_.close();
        } catch (IOException var19) {
            IOException lvt_7_2_ = var19;
            lvt_7_2_.printStackTrace();
        }

    }

    private void addRegionFilesToCollection(File p_75810_1_, Collection<File> p_75810_2_) {
        File lvt_3_1_ = new File(p_75810_1_, "region");
        File[] lvt_4_1_ = lvt_3_1_.listFiles(new FilenameFilter() {
            public boolean accept(File p_accept_1_, String p_accept_2_) {
                return p_accept_2_.endsWith(".mcr");
            }
        });
        if (lvt_4_1_ != null) {
            Collections.addAll(p_75810_2_, lvt_4_1_);
        }

    }
}
