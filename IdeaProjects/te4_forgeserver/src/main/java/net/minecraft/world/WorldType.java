//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\ExtraApps\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package net.minecraft.world;

import net.minecraftforge.fml.relauncher.*;
import net.minecraft.init.*;
import net.minecraft.world.biome.*;
import java.util.*;
import net.minecraft.server.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.layer.*;
import plus.fserver.world.generator.biome.BiomeProviderEx;

public class WorldType
{
    public static WorldType[] WORLD_TYPES;
    public static final WorldType DEFAULT;
    public static final WorldType FLAT;
    public static final WorldType LARGE_BIOMES;
    public static final WorldType AMPLIFIED;
    public static final WorldType CUSTOMIZED;
    public static final WorldType DEBUG_ALL_BLOCK_STATES;
    public static final WorldType DEFAULT_1_1;
    private final int id;
    private final String name;
    private final int version;
    private boolean canBeCreated;
    private boolean versioned;
    private boolean hasInfoNotice;

    private WorldType(final int id, final String name) {
        this(id, name, 0);
    }

    private WorldType(final int id, final String name, final int version) {
        if (name.length() > 16 && WorldType.DEBUG_ALL_BLOCK_STATES != null) {
            throw new IllegalArgumentException("World type names must not be longer then 16: " + name);
        }
        this.name = name;
        this.version = version;
        this.canBeCreated = true;
        this.id = id;
        WorldType.WORLD_TYPES[id] = this;
    }

    public String getName() {
        return this.name;
    }

    @SideOnly(Side.CLIENT)
    public String getTranslationKey() {
        return "generator." + this.name;
    }

    @SideOnly(Side.CLIENT)
    public String getInfoTranslationKey() {
        return this.getTranslationKey() + ".info";
    }

    public int getVersion() {
        return this.version;
    }

    public WorldType getWorldTypeForGeneratorVersion(final int version) {
        return (this == WorldType.DEFAULT && version == 0) ? WorldType.DEFAULT_1_1 : this;
    }

    private WorldType setCanBeCreated(final boolean enable) {
        this.canBeCreated = enable;
        return this;
    }

    @SideOnly(Side.CLIENT)
    public boolean canBeCreated() {
        return this.canBeCreated;
    }

    private WorldType setVersioned() {
        this.versioned = true;
        return this;
    }

    public boolean isVersioned() {
        return this.versioned;
    }

    public static WorldType byName(final String type) {
        for (final WorldType worldtype : WorldType.WORLD_TYPES) {
            if (worldtype != null && worldtype.name.equalsIgnoreCase(type)) {
                return worldtype;
            }
        }
        return null;
    }

    public int getId() {
        return this.id;
    }

    @SideOnly(Side.CLIENT)
    public boolean hasInfoNotice() {
        return this.hasInfoNotice;
    }

    private WorldType enableInfoNotice() {
        this.hasInfoNotice = true;
        return this;
    }


    public BiomeProvider getBiomeProvider(final World world) {
        if (this == WorldType.FLAT) {
            final FlatGeneratorInfo flatgeneratorinfo = FlatGeneratorInfo.createFlatGeneratorFromString(world.getWorldInfo().getGeneratorOptions());
            return new BiomeProviderSingle(Biome.getBiome(flatgeneratorinfo.getBiome(), Biomes.DEFAULT));
        }
        if (this == WorldType.DEBUG_ALL_BLOCK_STATES) {
            return new BiomeProviderSingle(Biomes.PLAINS);
        }
        return new BiomeProviderEx(world, world.getWorldInfo(), null);
    }


    public IChunkGenerator getChunkGenerator(final World p_getChunkGenerator_1_, final String p_getChunkGenerator_2_) {
        if (this == WorldType.FLAT) {
            return (IChunkGenerator)new ChunkGeneratorFlat(p_getChunkGenerator_1_, p_getChunkGenerator_1_.getSeed(), p_getChunkGenerator_1_.getWorldInfo().isMapFeaturesEnabled(), p_getChunkGenerator_2_);
        }
        if (this == WorldType.DEBUG_ALL_BLOCK_STATES) {
            return (IChunkGenerator)new ChunkGeneratorDebug(p_getChunkGenerator_1_);
        }
        if (this == WorldType.CUSTOMIZED) {
            return (IChunkGenerator)new ChunkGeneratorOverworld(p_getChunkGenerator_1_, p_getChunkGenerator_1_.getSeed(), p_getChunkGenerator_1_.getWorldInfo().isMapFeaturesEnabled(), p_getChunkGenerator_2_);
        }
        return (IChunkGenerator)new ChunkGeneratorOverworld(p_getChunkGenerator_1_, p_getChunkGenerator_1_.getSeed(), p_getChunkGenerator_1_.getWorldInfo().isMapFeaturesEnabled(), p_getChunkGenerator_2_);
    }

    public int getMinimumSpawnHeight(final World p_getMinimumSpawnHeight_1_) {
        return (this == WorldType.FLAT) ? 4 : (p_getMinimumSpawnHeight_1_.getSeaLevel() + 1);
    }

    public double getHorizon(final World p_getHorizon_1_) {
        return (this == WorldType.FLAT) ? 0.0 : 63.0;
    }

    public double voidFadeMagnitude() {
        return (this == WorldType.FLAT) ? 1.0 : 0.03125;
    }

    public boolean handleSlimeSpawnReduction(final Random p_handleSlimeSpawnReduction_1_, final World p_handleSlimeSpawnReduction_2_) {
        return this == WorldType.FLAT && p_handleSlimeSpawnReduction_1_.nextInt(4) != 1;
    }

    private static int getNextID() {
        for (int x = 0; x < WorldType.WORLD_TYPES.length; ++x) {
            if (WorldType.WORLD_TYPES[x] == null) {
                return x;
            }
        }
        final int oldLen = WorldType.WORLD_TYPES.length;
        WorldType.WORLD_TYPES = Arrays.copyOf(WorldType.WORLD_TYPES, oldLen + 16);
        return oldLen;
    }

    public WorldType(final String p_i47630_1_) {
        this(getNextID(), p_i47630_1_);
    }

    public void onGUICreateWorldPress() {
    }

    public int getSpawnFuzz(final WorldServer p_getSpawnFuzz_1_, final MinecraftServer p_getSpawnFuzz_2_) {
        return Math.max(0, p_getSpawnFuzz_2_.getSpawnRadius(p_getSpawnFuzz_1_));
    }

    @SideOnly(Side.CLIENT)
    public void onCustomizeButton(final Minecraft p_onCustomizeButton_1_, final GuiCreateWorld p_onCustomizeButton_2_) {
        if (this == WorldType.FLAT) {
            p_onCustomizeButton_1_.displayGuiScreen((GuiScreen)new GuiCreateFlatWorld(p_onCustomizeButton_2_, p_onCustomizeButton_2_.chunkProviderSettingsJson));
        }
        else if (this == WorldType.CUSTOMIZED) {
            p_onCustomizeButton_1_.displayGuiScreen((GuiScreen)new GuiCustomizeWorldScreen((GuiScreen)p_onCustomizeButton_2_, p_onCustomizeButton_2_.chunkProviderSettingsJson));
        }
    }

    public boolean isCustomizable() {
        return this == WorldType.FLAT || this == WorldType.CUSTOMIZED;
    }

    public float getCloudHeight() {
        return 128.0f;
    }

    public GenLayer getBiomeLayer(final long p_getBiomeLayer_1_, final GenLayer p_getBiomeLayer_3_, final ChunkGeneratorSettings p_getBiomeLayer_4_) {
        GenLayer ret = (GenLayer)new GenLayerBiome(200L, p_getBiomeLayer_3_, this, p_getBiomeLayer_4_);
        ret = GenLayerZoom.magnify(1000L, ret, 2);
        ret = (GenLayer)new GenLayerBiomeEdge(1000L, ret);
        return ret;
    }

    static {
        WorldType.WORLD_TYPES = new WorldType[16];
        DEFAULT = new WorldType(0, "default", 1).setVersioned();
        FLAT = new WorldType(1, "flat");
        LARGE_BIOMES = new WorldType(2, "largeBiomes");
        AMPLIFIED = new WorldType(3, "amplified").enableInfoNotice();
        CUSTOMIZED = new WorldType(4, "customized");
        DEBUG_ALL_BLOCK_STATES = new WorldType(5, "debug_all_block_states");
        DEFAULT_1_1 = new WorldType(8, "default_1_1", 0).setCanBeCreated(false);
    }
}