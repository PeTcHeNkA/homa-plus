package net.minecraft.world.chunk.storage;

import com.google.common.collect.Maps;
import plus.fserver.ElcaConfig;
import plus.fserver.world.save.IMapData;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;


public class RegionFileCache implements IMapData {
    private final Map<File, RegionFile> REGIONS_BY_FILE = Maps.newHashMap();

    public synchronized RegionFile createOrLoadRegionFile(File dir, int x, int z) {
        File subDir = new File(dir, "region");
        File lvt_4_1_ = getFile(subDir, x >> 5, z >> 5);
        RegionFile lvt_5_1_ = REGIONS_BY_FILE.get(lvt_4_1_);
        if (lvt_5_1_ != null) {
            return lvt_5_1_;
        } else {
            if (!subDir.exists()) {
                subDir.mkdirs();
            }

            if (REGIONS_BY_FILE.size() >= ElcaConfig.MAP_MAX_OPEN_FILES) {
                clearRegionFileReferences();
            }

            RegionFile lvt_6_1_ = new RegionFile(lvt_4_1_);
            REGIONS_BY_FILE.put(lvt_4_1_, lvt_6_1_);
            return lvt_6_1_;
        }
    }


    public static File getFile(File subDir, int x, int z){
        return new File(subDir, "r." + (x >> 5) + "." + (z >> 5) + ".mca");
    }


    public synchronized RegionFile getRegionFileIfExists(File dir, int x, int z) {
        File subDir = new File(dir, "region");
        File lvt_4_1_ = getFile(subDir, x >> 5, z >> 5);
        RegionFile lvt_5_1_ = REGIONS_BY_FILE.get(lvt_4_1_);
        if (lvt_5_1_ != null) {
            return lvt_5_1_;
        } else if (subDir.exists() && lvt_4_1_.exists()) {
            if (REGIONS_BY_FILE.size() >= 256) {
                clearRegionFileReferences();
            }
            RegionFile lvt_6_1_ = new RegionFile(lvt_4_1_);
            REGIONS_BY_FILE.put(lvt_4_1_, lvt_6_1_);
            return lvt_6_1_;
        } else {
            return null;
        }
    }


    public synchronized void clearRegionFileReferences() {
        Iterator var0 = REGIONS_BY_FILE.values().iterator();
        while(var0.hasNext()) {
            RegionFile lvt_1_1_ = (RegionFile)var0.next();
            try {
                if (lvt_1_1_ != null) {
                    lvt_1_1_.close();
                }
            } catch (IOException var3) {
                IOException lvt_2_1_ = var3;
                lvt_2_1_.printStackTrace();
            }
        }
        REGIONS_BY_FILE.clear();
    }


    public DataInputStream getChunkInputStream(File p_76549_0_, int p_76549_1_, int p_76549_2_) {
        RegionFile lvt_3_1_ = createOrLoadRegionFile(p_76549_0_, p_76549_1_, p_76549_2_);
        return lvt_3_1_.getChunkDataInputStream(p_76549_1_ & 31, p_76549_2_ & 31);
    }


    public DataOutputStream getChunkOutputStream(File p_76552_0_, int p_76552_1_, int p_76552_2_) {
        RegionFile lvt_3_1_ = createOrLoadRegionFile(p_76552_0_, p_76552_1_, p_76552_2_);
        return lvt_3_1_.getChunkDataOutputStream(p_76552_1_ & 31, p_76552_2_ & 31);
    }


    public boolean chunkExists(File p_191064_0_, int p_191064_1_, int p_191064_2_) {
        RegionFile lvt_3_1_ = getRegionFileIfExists(p_191064_0_, p_191064_1_, p_191064_2_);
        return lvt_3_1_ != null ? lvt_3_1_.isChunkSaved(p_191064_1_ & 31, p_191064_2_ & 31) : false;
    }
}