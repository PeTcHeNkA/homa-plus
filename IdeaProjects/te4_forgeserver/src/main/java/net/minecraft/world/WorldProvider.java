package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.api.ElcaWorldProvider;


public abstract class WorldProvider implements ElcaWorldProvider {
    public static final float[] MOON_PHASE_FACTORS = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
    protected World world;
    private WorldType terrainType;
    private String generatorSettings;
    protected BiomeProvider biomeProvider;
    protected boolean doesWaterVaporize;
    protected boolean nether;
    protected boolean hasSkyLight;
    protected final float[] lightBrightnessTable = new float[16];

    @SideOnly(Side.CLIENT)
    private float[] colorsSunriseSunset;
    @SideOnly(Side.CLIENT)
    private IRenderHandler skyRenderer;
    @SideOnly(Side.CLIENT)
    private IRenderHandler cloudRenderer;
    @SideOnly(Side.CLIENT)
    private IRenderHandler weatherRenderer;
    private int dimensionId;

    public WorldProvider() {}


    public final void setWorld(World world) {
        this.world = world;
        this.terrainType = world.getWorldInfo().getTerrainType();
        this.generatorSettings = world.getWorldInfo().getGeneratorOptions();
        this.init();
        this.generateLightBrightnessTable();
    }


    protected void generateLightBrightnessTable() {
        for(int i = 0; i <= 15; ++i) {
            float f1 = 1.0F - (float)i / 15.0F;
            this.lightBrightnessTable[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * 1.0F + 0.0F;
        }
    }


    protected void init() {
        this.hasSkyLight = true;
        this.biomeProvider = this.terrainType.getBiomeProvider(this.world);
    }


    @Override
    public void setBiomeProvider(BiomeProvider provider){
        this.biomeProvider = provider;
    }


    public IChunkGenerator createChunkGenerator() {
        return this.terrainType.getChunkGenerator(this.world, this.generatorSettings);
    }


    public boolean canCoordinateBeSpawn(int p_76566_1_, int p_76566_2_) {
        BlockPos blockpos = new BlockPos(p_76566_1_, 0, p_76566_2_);
        if (this.world.getBiome(blockpos).ignorePlayerSpawnSuitability()) {
            return true;
        } else {
            return this.world.getGroundAboveSeaLevel(blockpos).getBlock() == Blocks.GRASS;
        }
    }


    public float calculateCelestialAngle(long p_76563_1_, float p_76563_3_) {
        int i = (int)(p_76563_1_ % 24000L);
        float f = ((float)i + p_76563_3_) / 24000.0F - 0.25F;

        if (f < 0.0F) ++f;
        if (f > 1.0F) --f;

        float f1 = 1.0F - (float)((Math.cos((double)f * Math.PI) + 1.0) / 2.0);
        f += (f1 - f) / 3.0F;
        return f;
    }

    public int getMoonPhase(long p_76559_1_) {
        return (int)(p_76559_1_ / 24000L % 8L + 8L) % 8;
    }


    public boolean isSurfaceWorld() {
        return true;
    }


    @Nullable
    @SideOnly(Side.CLIENT)
    public float[] calcSunriseSunsetColors(float fin1, float fin2) {
        float f1 = MathHelper.cos(fin1 * 6.2831855F) - 0.0F;
        if (f1 >= -0.4F && f1 <= 0.4F) {
            float[] floats;
            if((floats = colorsSunriseSunset) == null)floats = colorsSunriseSunset = new float[4];

            float f3 = (f1 - -0.0F) / 0.4F * 0.5F + 0.5F;
            float f4 = 1.0F - (1.0F - MathHelper.sin(f3 * 3.1415927F)) * 0.99F;
            f4 *= f4;
            floats[0] = f3 * 0.3F + 0.7F;
            floats[1] = f3 * f3 * 0.7F + 0.2F;
            floats[2] = f3 * f3 * 0.0F + 0.2F;
            floats[3] = f4;
            return floats;
        } else {
            return null;
        }
    }


    @SideOnly(Side.CLIENT)
    public Vec3d getFogColor(float p_76562_1_, float p_76562_2_) {
        float f = MathHelper.cos(p_76562_1_ * 6.2831855F) * 2.0F + 0.5F;
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        float f1 = 0.7529412F;
        float f2 = 0.84705883F;
        float f3 = 1.0F;
        f1 *= f * 0.94F + 0.06F;
        f2 *= f * 0.94F + 0.06F;
        f3 *= f * 0.91F + 0.09F;
        return new Vec3d(f1, f2, f3);
    }


    public boolean canRespawnHere() {
        return true;
    }


    @SideOnly(Side.CLIENT)
    public float getCloudHeight() {
        return this.terrainType.getCloudHeight();
    }


    @SideOnly(Side.CLIENT)
    public boolean isSkyColored() {
        return true;
    }


    @Nullable
    public BlockPos getSpawnCoordinate() {
        return null;
    }


    public int getAverageGroundLevel() {
        return this.terrainType.getMinimumSpawnHeight(this.world);
    }


    @SideOnly(Side.CLIENT)
    public double getVoidFogYFactor() {
        return this.terrainType.voidFadeMagnitude();
    }


    @SideOnly(Side.CLIENT)
    public boolean doesXZShowFog(int p_76568_1_, int p_76568_2_) {
        return false;
    }


    public BiomeProvider getBiomeProvider() {
        return this.biomeProvider;
    }


    public boolean doesWaterVaporize() {
        return this.doesWaterVaporize;
    }


    public boolean hasSkyLight() {
        return this.hasSkyLight;
    }


    public boolean isNether() {
        return this.nether;
    }


    public float[] getLightBrightnessTable() {
        return this.lightBrightnessTable;
    }


    public WorldBorder createWorldBorder() {
        return new WorldBorder();
    }


    public void setDimension(int id) {
        this.dimensionId = id;
    }


    public int getDimension() {
        return this.dimensionId;
    }


    @Nullable
    public String getSaveFolder() {
        return this.dimensionId == 0 ? null : "DIM" + this.dimensionId;
    }


    public double getMovementFactor() {
        return this instanceof WorldProviderHell ? 8.0 : 1.0;
    }


    public boolean shouldClientCheckLighting() {
        return !(this instanceof WorldProviderSurface);
    }


    @Nullable
    @SideOnly(Side.CLIENT)
    public IRenderHandler getSkyRenderer() {
        return this.skyRenderer;
    }


    @SideOnly(Side.CLIENT)
    public void setSkyRenderer(IRenderHandler p_setSkyRenderer_1_) {
        this.skyRenderer = p_setSkyRenderer_1_;
    }


    @Nullable
    @SideOnly(Side.CLIENT)
    public IRenderHandler getCloudRenderer() {
        return this.cloudRenderer;
    }


    @SideOnly(Side.CLIENT)
    public void setCloudRenderer(IRenderHandler p_setCloudRenderer_1_) {
        this.cloudRenderer = p_setCloudRenderer_1_;
    }


    @Nullable
    @SideOnly(Side.CLIENT)
    public IRenderHandler getWeatherRenderer() {
        return this.weatherRenderer;
    }


    @SideOnly(Side.CLIENT)
    public void setWeatherRenderer(IRenderHandler p_setWeatherRenderer_1_) {
        this.weatherRenderer = p_setWeatherRenderer_1_;
    }


    public void getLightmapColors(float p_getLightmapColors_1_, float p_getLightmapColors_2_, float p_getLightmapColors_3_, float p_getLightmapColors_4_, float[] p_getLightmapColors_5_) {
    }


    public BlockPos getRandomizedSpawnPoint() {
        BlockPos ret = this.world.getSpawnPoint();
        boolean isAdventure = this.world.getWorldInfo().getGameType() == GameType.ADVENTURE;
        int spawnFuzz = this.world instanceof WorldServer ? this.terrainType.getSpawnFuzz((WorldServer)this.world, this.world.getMinecraftServer()) : 1;
        int border = MathHelper.floor(this.world.getWorldBorder().getClosestDistance(ret.getX(), ret.getZ()));
        if (border < spawnFuzz) {
            spawnFuzz = border;
        }

        if (!this.isNether() && !isAdventure && spawnFuzz != 0) {
            if (spawnFuzz < 2) {
                spawnFuzz = 2;
            }

            int spawnFuzzHalf = spawnFuzz / 2;
            ret = this.world.getTopSolidOrLiquidBlock(
                    ret.add(spawnFuzzHalf - this.world.rand.nextInt(spawnFuzz), 0, spawnFuzzHalf - this.world.rand.nextInt(spawnFuzz))
            );
        }
        return ret;
    }

    public boolean shouldMapSpin(String p_shouldMapSpin_1_, double p_shouldMapSpin_2_, double p_shouldMapSpin_4_, double p_shouldMapSpin_6_) {
        return this.dimensionId < 0;
    }


    public int getRespawnDimension(EntityPlayerMP player) {
        return player.getSpawnDimension();
    }


    @Nullable
    public ICapabilityProvider initCapabilities() {
        return null;
    }


    @Nullable
    @SideOnly(Side.CLIENT)
    public MusicTicker.MusicType getMusicType() {
        return null;
    }


    public WorldSleepResult canSleepAt(EntityPlayer p_canSleepAt_1_, BlockPos p_canSleepAt_2_) {
        return this.canRespawnHere() && this.world.getBiome(p_canSleepAt_2_) != Biomes.HELL ? WorldProvider.WorldSleepResult.ALLOW : WorldProvider.WorldSleepResult.BED_EXPLODES;
    }


    public Biome getBiomeForCoords(BlockPos p_getBiomeForCoords_1_) {
        return this.world.getBiomeForCoordsBody(p_getBiomeForCoords_1_);
    }


    public boolean isDaytime() {
        return this.world.getSkylightSubtracted() < 4;
    }


    public float getSunBrightnessFactor(float p_getSunBrightnessFactor_1_) {
        return this.world.getSunBrightnessFactor(p_getSunBrightnessFactor_1_);
    }


    public float getCurrentMoonPhaseFactor() {
        return this.world.getCurrentMoonPhaseFactorBody();
    }


    @SideOnly(Side.CLIENT)
    public Vec3d getSkyColor(Entity p_getSkyColor_1_, float p_getSkyColor_2_) {
        return this.world.getSkyColorBody(p_getSkyColor_1_, p_getSkyColor_2_);
    }


    @SideOnly(Side.CLIENT)
    public Vec3d getCloudColor(float p_getCloudColor_1_) {
        return this.world.getCloudColorBody(p_getCloudColor_1_);
    }


    @SideOnly(Side.CLIENT)
    public float getSunBrightness(float p_getSunBrightness_1_) {
        return this.world.getSunBrightnessBody(p_getSunBrightness_1_);
    }


    @SideOnly(Side.CLIENT)
    public float getStarBrightness(float p_getStarBrightness_1_) {
        return this.world.getStarBrightnessBody(p_getStarBrightness_1_);
    }


    public void setAllowedSpawnTypes(boolean p_setAllowedSpawnTypes_1_, boolean p_setAllowedSpawnTypes_2_) {
        this.world.spawnHostileMobs = p_setAllowedSpawnTypes_1_;
        this.world.spawnPeacefulMobs = p_setAllowedSpawnTypes_2_;
    }


    public void calculateInitialWeather() {
        this.world.calculateInitialWeatherBody();
    }


    public void updateWeather() {
        this.world.updateWeatherBody();
    }


    public boolean canBlockFreeze(BlockPos p_canBlockFreeze_1_, boolean p_canBlockFreeze_2_) {
        return this.world.canBlockFreezeBody(p_canBlockFreeze_1_, p_canBlockFreeze_2_);
    }


    public boolean canSnowAt(BlockPos p_canSnowAt_1_, boolean p_canSnowAt_2_) {
        return this.world.canSnowAtBody(p_canSnowAt_1_, p_canSnowAt_2_);
    }


    public void setWorldTime(long p_setWorldTime_1_) {
        this.world.worldInfo.setWorldTime(p_setWorldTime_1_);
    }


    public long getSeed() {
        return this.world.worldInfo.getSeed();
    }


    public long getWorldTime() {
        return this.world.worldInfo.getWorldTime();
    }


    public BlockPos getSpawnPoint() {
        WorldInfo info = this.world.worldInfo;
        return new BlockPos(info.getSpawnX(), info.getSpawnY(), info.getSpawnZ());
    }


    public void setSpawnPoint(BlockPos pos) {
        this.world.worldInfo.setSpawn(pos);
    }


    public boolean canMineBlock(EntityPlayer p_canMineBlock_1_, BlockPos p_canMineBlock_2_) {
        return this.world.canMineBlockBody(p_canMineBlock_1_, p_canMineBlock_2_);
    }


    public boolean isBlockHighHumidity(BlockPos pos) {
        return this.world.getBiome(pos).isHighHumidity();
    }


    public int getHeight() {
        return 256;
    }


    public int getActualHeight() {
        return this.nether ? 128 : 256;
    }


    public double getHorizon() {
        return this.world.worldInfo.getTerrainType().getHorizon(this.world);
    }


    public void resetRainAndThunder() {
        this.world.worldInfo.setRainTime(0);
        this.world.worldInfo.setRaining(false);
        this.world.worldInfo.setThunderTime(0);
        this.world.worldInfo.setThundering(false);
    }


    public boolean canDoLightning(Chunk chunk) {
        return true;
    }


    public boolean canDoRainSnowIce(Chunk chunk) {
        return true;
    }


    public void onPlayerAdded(EntityPlayerMP player) {}


    public void onPlayerRemoved(EntityPlayerMP player) {}


    public abstract DimensionType getDimensionType();


    public void onWorldSave() {}


    public void onWorldUpdateEntities() {}


    public boolean canDropChunk(int x, int z) {
        return true;
    }


    public static enum WorldSleepResult {
        ALLOW,
        DENY,
        BED_EXPLODES;

        private WorldSleepResult() {}
    }
}
