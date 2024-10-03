package net.minecraft.world.biome;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockFlower.EnumFlowerType;
import net.minecraft.block.BlockTallGrass.EnumType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.init.Blocks;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeHills.Type;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenDoublePlant;
import net.minecraft.world.gen.feature.WorldGenSwamp;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.event.terraingen.DeferredBiomeDecorator;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.api.Keep;


public abstract class Biome extends IForgeRegistryEntry.Impl<Biome> {
    private static final Logger LOGGER = LogManager.getLogger();
    @Keep
    protected static final IBlockState STONE;
    @Keep
    protected static final IBlockState AIR;
    @Keep
    protected static final IBlockState BEDROCK;
    @Keep
    protected static final IBlockState GRAVEL;
    @Keep
    protected static final IBlockState RED_SANDSTONE;
    @Keep
    protected static final IBlockState SANDSTONE;
    @Keep
    protected static final IBlockState ICE;
    @Keep
    protected static final IBlockState WATER;
    @Keep
    public static final ObjectIntIdentityMap<Biome> MUTATION_TO_BASE_ID_MAP;
    @Keep
    protected static final NoiseGeneratorPerlin TEMPERATURE_NOISE;
    @Keep
    protected static final NoiseGeneratorPerlin GRASS_COLOR_NOISE;
    @Keep
    protected static final WorldGenDoublePlant DOUBLE_PLANT_GENERATOR;
    @Keep
    protected static final WorldGenTrees TREE_FEATURE;
    @Keep
    protected static final WorldGenBigTree BIG_TREE_FEATURE;
    @Keep
    protected static final WorldGenSwamp SWAMP_FEATURE;
    @Keep
    public static final RegistryNamespaced<ResourceLocation, Biome> REGISTRY;
    private final String biomeName;
    private final float baseHeight;
    private final float heightVariation;
    private final float temperature;
    private final float rainfall;
    private final int waterColor;
    private final boolean enableSnow;
    private final boolean enableRain;
    @Nullable
    private final String baseBiomeRegName;
    @Keep
    public IBlockState topBlock;
    @Keep
    public IBlockState fillerBlock;
    @Keep
    public BiomeDecorator decorator;
    @Keep
    protected List<SpawnListEntry> spawnableMonsterList;
    @Keep
    protected List<SpawnListEntry> spawnableCreatureList;
    @Keep
    protected List<SpawnListEntry> spawnableWaterCreatureList;
    @Keep
    protected List<SpawnListEntry> spawnableCaveCreatureList;
    @Keep
    protected Map<EnumCreatureType, List<SpawnListEntry>> modSpawnableLists;
    @Keep
    protected List<FlowerEntry> flowers;
    private int registryBiomeId = -1;

    public static int getIdForBiome(Biome biome) {
        int id = biome.registryBiomeId;
        if(id == -1)return biome.registryBiomeId = REGISTRY.getIDForObject(biome);
        return id;
    }


    @Nullable
    public static Biome getBiomeForId(int id) {
        return REGISTRY.getObjectById(id);
    }


    @Nullable
    public static Biome getMutationForBiome(Biome biome) {
        return MUTATION_TO_BASE_ID_MAP.getByValue(getIdForBiome(biome));
    }


    public Biome(BiomeProperties properties) {
        this.topBlock = Blocks.GRASS.getDefaultState();
        this.fillerBlock = Blocks.DIRT.getDefaultState();
        this.spawnableMonsterList = Lists.newArrayList();
        this.spawnableCreatureList = Lists.newArrayList();
        this.spawnableWaterCreatureList = Lists.newArrayList();
        this.spawnableCaveCreatureList = Lists.newArrayList();
        this.modSpawnableLists = Maps.newHashMap();
        this.flowers = new ArrayList();
        this.biomeName = properties.biomeName;
        this.baseHeight = properties.baseHeight;
        this.heightVariation = properties.heightVariation;
        this.temperature = properties.temperature;
        this.rainfall = properties.rainfall;
        this.waterColor = properties.waterColor;
        this.enableSnow = properties.enableSnow;
        this.enableRain = properties.enableRain;
        this.baseBiomeRegName = properties.baseBiomeRegName;
        this.decorator = this.createBiomeDecorator();
        this.spawnableCreatureList.add(new SpawnListEntry(EntitySheep.class, 12, 4, 4));
        this.spawnableCreatureList.add(new SpawnListEntry(EntityPig.class, 10, 4, 4));
        this.spawnableCreatureList.add(new SpawnListEntry(EntityChicken.class, 10, 4, 4));
        this.spawnableCreatureList.add(new SpawnListEntry(EntityCow.class, 8, 4, 4));
        this.spawnableMonsterList.add(new SpawnListEntry(EntitySpider.class, 100, 4, 4));
        this.spawnableMonsterList.add(new SpawnListEntry(EntityZombie.class, 95, 4, 4));
        this.spawnableMonsterList.add(new SpawnListEntry(EntityZombieVillager.class, 5, 1, 1));
        this.spawnableMonsterList.add(new SpawnListEntry(EntitySkeleton.class, 100, 4, 4));
        this.spawnableMonsterList.add(new SpawnListEntry(EntityCreeper.class, 100, 4, 4));
        this.spawnableMonsterList.add(new SpawnListEntry(EntitySlime.class, 100, 4, 4));
        this.spawnableMonsterList.add(new SpawnListEntry(EntityEnderman.class, 10, 1, 4));
        this.spawnableMonsterList.add(new SpawnListEntry(EntityWitch.class, 5, 1, 1));
        this.spawnableWaterCreatureList.add(new SpawnListEntry(EntitySquid.class, 10, 4, 4));
        this.spawnableCaveCreatureList.add(new SpawnListEntry(EntityBat.class, 10, 8, 8));
        this.addDefaultFlowers();
    }


    public BiomeDecorator createBiomeDecorator() {
        return this.getModdedBiomeDecorator(new BiomeDecorator());
    }


    public boolean isMutation() {
        return this.baseBiomeRegName != null;
    }


    public WorldGenAbstractTree getRandomTreeFeature(Random rand) {
        return rand.nextInt(10) == 0 ? BIG_TREE_FEATURE : TREE_FEATURE;
    }


    public WorldGenerator getRandomWorldGenForGrass(Random p_76730_1_) {
        return new WorldGenTallGrass(EnumType.GRASS);
    }


    public BlockFlower.EnumFlowerType pickRandomFlower(Random rand, BlockPos pos) {
        return rand.nextInt(3) > 0 ? EnumFlowerType.DANDELION : EnumFlowerType.POPPY;
    }


    @SideOnly(Side.CLIENT)
    public int getSkyColorByTemp(float p_76731_1_) {
        p_76731_1_ /= 3.0F;
        p_76731_1_ = MathHelper.clamp(p_76731_1_, -1.0F, 1.0F);
        return MathHelper.hsvToRGB(0.62222224F - p_76731_1_ * 0.05F, 0.5F + p_76731_1_ * 0.1F, 1.0F);
    }


    public List<SpawnListEntry> getSpawnableList(EnumCreatureType type) {
        switch (type) {
            case MONSTER:
                return this.spawnableMonsterList;
            case CREATURE:
                return this.spawnableCreatureList;
            case WATER_CREATURE:
                return this.spawnableWaterCreatureList;
            case AMBIENT:
                return this.spawnableCaveCreatureList;
            default:
                if (!this.modSpawnableLists.containsKey(type)) {
                    this.modSpawnableLists.put(type, Lists.newArrayList());
                }
                return this.modSpawnableLists.get(type);
        }
    }


    public boolean getEnableSnow() {
        return this.isSnowyBiome();
    }


    public boolean canRain() {
        return this.isSnowyBiome() ? false : this.enableRain;
    }


    public boolean isHighHumidity() {
        return this.getRainfall() > 0.85F;
    }


    public float getSpawningChance() {
        return 0.1F;
    }


    public final float getTemperature(BlockPos pos) {
        if (pos.getY() > 64) {
            float f = (float)(TEMPERATURE_NOISE.getValue((float)pos.getX() / 8.0F, (float)pos.getZ() / 8.0F) * 4.0);
            return this.getDefaultTemperature() - (f + (float)pos.getY() - 64.0F) * 0.05F / 30.0F;
        } else {
            return this.getDefaultTemperature();
        }
    }


    public void decorate(World world, Random rand, BlockPos pos) {
        this.decorator.decorate(world, rand, this, pos);
    }


    public void genTerrainBlocks(World world, Random rand, ChunkPrimer primer, int x, int z, double num) {
        this.generateBiomeTerrain(world, rand, primer, x, z, num);
    }


    @SideOnly(Side.CLIENT)
    public int getGrassColorAtPos(BlockPos pos) {
        double d0 = MathHelper.clamp(this.getTemperature(pos), 0.0F, 1.0F);
        double d1 = MathHelper.clamp(this.getRainfall(), 0.0F, 1.0F);
        return this.getModdedBiomeGrassColor(ColorizerGrass.getGrassColor(d0, d1));
    }


    public final void generateBiomeTerrain(World world, Random rand, ChunkPrimer primer, int bX, int bZ, double factor) {
        int sea = world.getSeaLevel();
        IBlockState top = this.topBlock;
        IBlockState fill = this.fillerBlock;
        int j = -1;
        int k = (int) (factor / 3.0 + 3.0 + rand.nextDouble() * 0.25);
        int x = bX & 15;
        int z = bZ & 15;
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

        for (int y = 255; y >= 0; --y) {
            if (y <= 5) {
                if(y <= rand.nextInt(5)) {
                    primer.setBlockState(z, y, x, BEDROCK);
                    continue;
                }
            }

            IBlockState cur = primer.getBlockState(z, y, x);
            if (cur.getMaterial() == Material.AIR) {
                j = -1;
            } else if (cur.getBlock() == Blocks.STONE) {
                if (j == -1) {
                    if (k <= 0) {
                        top = AIR;
                        fill = STONE;
                    } else if (y >= sea - 4 && y <= sea + 1) {
                        top = this.topBlock;
                        fill = this.fillerBlock;
                    }

                    if (y < sea && (top == null || top.getMaterial() == Material.AIR)) {
                        if (this.getTemperature(mut.setPos(bX, y, bZ)) < 0.15F) {
                            top = ICE;
                        } else {
                            top = WATER;
                        }
                    }

                    j = k;
                    if (y >= sea - 1) {
                        primer.setBlockState(z, y, x, top);
                    } else if (y < sea - 7 - k) {
                        top = AIR;
                        fill = STONE;
                        primer.setBlockState(z, y, x, GRAVEL);
                    } else {
                        primer.setBlockState(z, y, x, fill);
                    }
                } else if (j > 0) {
                    --j;
                    primer.setBlockState(z, y, x, fill);
                    if (j == 0 && fill.getBlock() == Blocks.SAND && k > 1) {
                        j = rand.nextInt(4) + Math.max(0, y - 63);
                        fill = fill.getValue(BlockSand.VARIANT) == net.minecraft.block.BlockSand.EnumType.RED_SAND ? RED_SANDSTONE : SANDSTONE;
                    }
                }
            }
        }
    }


    @SideOnly(Side.CLIENT)
    public int getFoliageColorAtPos(BlockPos pos) {
        double d0 = MathHelper.clamp(this.getTemperature(pos), 0.0F, 1.0F);
        double d1 = MathHelper.clamp(this.getRainfall(), 0.0F, 1.0F);
        return this.getModdedBiomeFoliageColor(ColorizerFoliage.getFoliageColor(d0, d1));
    }


    @Keep
    public Class<? extends Biome> getBiomeClass() {
        return this.getClass();
    }


    public TempCategory getTempCategory() {
        if ((double)this.getDefaultTemperature() < 0.2) {
            return Biome.TempCategory.COLD;
        } else {
            return (double)this.getDefaultTemperature() < 1.0 ? Biome.TempCategory.MEDIUM : Biome.TempCategory.WARM;
        }
    }


    @Nullable
    public static Biome getBiome(int id) {
        return getBiome(id, (Biome)null);
    }


    public static Biome getBiome(int id, Biome def) {
        Biome biome = getBiomeForId(id);
        return biome == null ? def : biome;
    }


    public boolean ignorePlayerSpawnSuitability() {
        return false;
    }


    public final float getBaseHeight() {
        return this.baseHeight;
    }


    public final float getRainfall() {
        return this.rainfall;
    }


    @SideOnly(Side.CLIENT)
    public final String getBiomeName() {
        return this.biomeName;
    }


    public final float getHeightVariation() {
        return this.heightVariation;
    }


    public final float getDefaultTemperature() {
        return this.temperature;
    }


    @SideOnly(Side.CLIENT)
    public final int getWaterColor() {
        return this.getWaterColorMultiplier();
    }


    public final boolean isSnowyBiome() {
        return this.enableSnow;
    }


    public BiomeDecorator getModdedBiomeDecorator(BiomeDecorator p_getModdedBiomeDecorator_1_) {
        return new DeferredBiomeDecorator(p_getModdedBiomeDecorator_1_);
    }


    public int getWaterColorMultiplier() {
        BiomeEvent.GetWaterColor event = new BiomeEvent.GetWaterColor(this, this.waterColor);
        MinecraftForge.EVENT_BUS.postF(event);
        return event.getNewColor();
    }


    public int getModdedBiomeGrassColor(int original) {
        BiomeEvent.GetGrassColor event = new BiomeEvent.GetGrassColor(this, original);
        MinecraftForge.EVENT_BUS.postF(event);
        return event.getNewColor();
    }


    public int getModdedBiomeFoliageColor(int original) {
        BiomeEvent.GetFoliageColor event = new BiomeEvent.GetFoliageColor(this, original);
        MinecraftForge.EVENT_BUS.postF(event);
        return event.getNewColor();
    }


    public void addDefaultFlowers() {
        this.addFlower(Blocks.YELLOW_FLOWER.getDefaultState().withProperty(Blocks.YELLOW_FLOWER.getTypeProperty(), EnumFlowerType.DANDELION), 20);
        this.addFlower(Blocks.RED_FLOWER.getDefaultState().withProperty(Blocks.RED_FLOWER.getTypeProperty(), EnumFlowerType.POPPY), 10);
    }


    public void addFlower(IBlockState state, int weight) {
        this.flowers.add(new FlowerEntry(state, weight));
    }


    public void plantFlower(World world, Random rand, BlockPos pos) {
        if (!this.flowers.isEmpty()) {
            FlowerEntry flower = WeightedRandom.getRandomItem(rand, this.flowers);
            if (flower != null && flower.state != null && (!(flower.state.getBlock() instanceof BlockBush) || ((BlockBush)flower.state.getBlock()).canBlockStay(world, pos, flower.state))) {
                world.setBlockState(pos, flower.state, 3);
            }
        }
    }


    @Keep
    public static void registerBiomes() {
        registerBiome(0, "ocean", new BiomeOcean((new BiomeProperties("Ocean")).setBaseHeight(-1.0F).setHeightVariation(0.1F)));
        registerBiome(1, "plains", new BiomePlains(false, (new BiomeProperties("Plains")).setBaseHeight(0.125F).setHeightVariation(0.05F).setTemperature(0.8F).setRainfall(0.4F)));
        registerBiome(2, "desert", new BiomeDesert((new BiomeProperties("Desert")).setBaseHeight(0.125F).setHeightVariation(0.05F).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
        registerBiome(3, "extreme_hills", new BiomeHills(Type.NORMAL, (new BiomeProperties("Extreme Hills")).setBaseHeight(1.0F).setHeightVariation(0.5F).setTemperature(0.2F).setRainfall(0.3F)));
        registerBiome(4, "forest", new BiomeForest(net.minecraft.world.biome.BiomeForest.Type.NORMAL, (new BiomeProperties("Forest")).setTemperature(0.7F).setRainfall(0.8F)));
        registerBiome(5, "taiga", new BiomeTaiga(net.minecraft.world.biome.BiomeTaiga.Type.NORMAL, (new BiomeProperties("Taiga")).setBaseHeight(0.2F).setHeightVariation(0.2F).setTemperature(0.25F).setRainfall(0.8F)));
        registerBiome(6, "swampland", new BiomeSwamp((new BiomeProperties("Swampland")).setBaseHeight(-0.2F).setHeightVariation(0.1F).setTemperature(0.8F).setRainfall(0.9F).setWaterColor(14745518)));
        registerBiome(7, "river", new BiomeRiver((new BiomeProperties("River")).setBaseHeight(-0.5F).setHeightVariation(0.0F)));
        registerBiome(8, "hell", new BiomeHell((new BiomeProperties("Hell")).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
        registerBiome(9, "sky", new BiomeEnd((new BiomeProperties("The End")).setRainDisabled()));
        registerBiome(10, "frozen_ocean", new BiomeOcean((new BiomeProperties("FrozenOcean")).setBaseHeight(-1.0F).setHeightVariation(0.1F).setTemperature(0.0F).setRainfall(0.5F).setSnowEnabled()));
        registerBiome(11, "frozen_river", new BiomeRiver((new BiomeProperties("FrozenRiver")).setBaseHeight(-0.5F).setHeightVariation(0.0F).setTemperature(0.0F).setRainfall(0.5F).setSnowEnabled()));
        registerBiome(12, "ice_flats", new BiomeSnow(false, (new BiomeProperties("Ice Plains")).setBaseHeight(0.125F).setHeightVariation(0.05F).setTemperature(0.0F).setRainfall(0.5F).setSnowEnabled()));
        registerBiome(13, "ice_mountains", new BiomeSnow(false, (new BiomeProperties("Ice Mountains")).setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(0.0F).setRainfall(0.5F).setSnowEnabled()));
        registerBiome(14, "mushroom_island", new BiomeMushroomIsland((new BiomeProperties("MushroomIsland")).setBaseHeight(0.2F).setHeightVariation(0.3F).setTemperature(0.9F).setRainfall(1.0F)));
        registerBiome(15, "mushroom_island_shore", new BiomeMushroomIsland((new BiomeProperties("MushroomIslandShore")).setBaseHeight(0.0F).setHeightVariation(0.025F).setTemperature(0.9F).setRainfall(1.0F)));
        registerBiome(16, "beaches", new BiomeBeach((new BiomeProperties("Beach")).setBaseHeight(0.0F).setHeightVariation(0.025F).setTemperature(0.8F).setRainfall(0.4F)));
        registerBiome(17, "desert_hills", new BiomeDesert((new BiomeProperties("DesertHills")).setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
        registerBiome(18, "forest_hills", new BiomeForest(net.minecraft.world.biome.BiomeForest.Type.NORMAL, (new BiomeProperties("ForestHills")).setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(0.7F).setRainfall(0.8F)));
        registerBiome(19, "taiga_hills", new BiomeTaiga(net.minecraft.world.biome.BiomeTaiga.Type.NORMAL, (new BiomeProperties("TaigaHills")).setTemperature(0.25F).setRainfall(0.8F).setBaseHeight(0.45F).setHeightVariation(0.3F)));
        registerBiome(20, "smaller_extreme_hills", new BiomeHills(Type.EXTRA_TREES, (new BiomeProperties("Extreme Hills Edge")).setBaseHeight(0.8F).setHeightVariation(0.3F).setTemperature(0.2F).setRainfall(0.3F)));
        registerBiome(21, "jungle", new BiomeJungle(false, (new BiomeProperties("Jungle")).setTemperature(0.95F).setRainfall(0.9F)));
        registerBiome(22, "jungle_hills", new BiomeJungle(false, (new BiomeProperties("JungleHills")).setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(0.95F).setRainfall(0.9F)));
        registerBiome(23, "jungle_edge", new BiomeJungle(true, (new BiomeProperties("JungleEdge")).setTemperature(0.95F).setRainfall(0.8F)));
        registerBiome(24, "deep_ocean", new BiomeOcean((new BiomeProperties("Deep Ocean")).setBaseHeight(-1.8F).setHeightVariation(0.1F)));
        registerBiome(25, "stone_beach", new BiomeStoneBeach((new BiomeProperties("Stone Beach")).setBaseHeight(0.1F).setHeightVariation(0.8F).setTemperature(0.2F).setRainfall(0.3F)));
        registerBiome(26, "cold_beach", new BiomeBeach((new BiomeProperties("Cold Beach")).setBaseHeight(0.0F).setHeightVariation(0.025F).setTemperature(0.05F).setRainfall(0.3F).setSnowEnabled()));
        registerBiome(27, "birch_forest", new BiomeForest(net.minecraft.world.biome.BiomeForest.Type.BIRCH, (new BiomeProperties("Birch Forest")).setTemperature(0.6F).setRainfall(0.6F)));
        registerBiome(28, "birch_forest_hills", new BiomeForest(net.minecraft.world.biome.BiomeForest.Type.BIRCH, (new BiomeProperties("Birch Forest Hills")).setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(0.6F).setRainfall(0.6F)));
        registerBiome(29, "roofed_forest", new BiomeForest(net.minecraft.world.biome.BiomeForest.Type.ROOFED, (new BiomeProperties("Roofed Forest")).setTemperature(0.7F).setRainfall(0.8F)));
        registerBiome(30, "taiga_cold", new BiomeTaiga(net.minecraft.world.biome.BiomeTaiga.Type.NORMAL, (new BiomeProperties("Cold Taiga")).setBaseHeight(0.2F).setHeightVariation(0.2F).setTemperature(-0.5F).setRainfall(0.4F).setSnowEnabled()));
        registerBiome(31, "taiga_cold_hills", new BiomeTaiga(net.minecraft.world.biome.BiomeTaiga.Type.NORMAL, (new BiomeProperties("Cold Taiga Hills")).setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(-0.5F).setRainfall(0.4F).setSnowEnabled()));
        registerBiome(32, "redwood_taiga", new BiomeTaiga(net.minecraft.world.biome.BiomeTaiga.Type.MEGA, (new BiomeProperties("Mega Taiga")).setTemperature(0.3F).setRainfall(0.8F).setBaseHeight(0.2F).setHeightVariation(0.2F)));
        registerBiome(33, "redwood_taiga_hills", new BiomeTaiga(net.minecraft.world.biome.BiomeTaiga.Type.MEGA, (new BiomeProperties("Mega Taiga Hills")).setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(0.3F).setRainfall(0.8F)));
        registerBiome(34, "extreme_hills_with_trees", new BiomeHills(Type.EXTRA_TREES, (new BiomeProperties("Extreme Hills+")).setBaseHeight(1.0F).setHeightVariation(0.5F).setTemperature(0.2F).setRainfall(0.3F)));
        registerBiome(35, "savanna", new BiomeSavanna((new BiomeProperties("Savanna")).setBaseHeight(0.125F).setHeightVariation(0.05F).setTemperature(1.2F).setRainfall(0.0F).setRainDisabled()));
        registerBiome(36, "savanna_rock", new BiomeSavanna((new BiomeProperties("Savanna Plateau")).setBaseHeight(1.5F).setHeightVariation(0.025F).setTemperature(1.0F).setRainfall(0.0F).setRainDisabled()));
        registerBiome(37, "mesa", new BiomeMesa(false, false, (new BiomeProperties("Mesa")).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
        registerBiome(38, "mesa_rock", new BiomeMesa(false, true, (new BiomeProperties("Mesa Plateau F")).setBaseHeight(1.5F).setHeightVariation(0.025F).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
        registerBiome(39, "mesa_clear_rock", new BiomeMesa(false, false, (new BiomeProperties("Mesa Plateau")).setBaseHeight(1.5F).setHeightVariation(0.025F).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
        registerBiome(127, "void", new BiomeVoid((new BiomeProperties("The Void")).setRainDisabled()));
        registerBiome(129, "mutated_plains", new BiomePlains(true, (new BiomeProperties("Sunflower Plains")).setBaseBiome("plains").setBaseHeight(0.125F).setHeightVariation(0.05F).setTemperature(0.8F).setRainfall(0.4F)));
        registerBiome(130, "mutated_desert", new BiomeDesert((new BiomeProperties("Desert M")).setBaseBiome("desert").setBaseHeight(0.225F).setHeightVariation(0.25F).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
        registerBiome(131, "mutated_extreme_hills", new BiomeHills(Type.MUTATED, (new BiomeProperties("Extreme Hills M")).setBaseBiome("extreme_hills").setBaseHeight(1.0F).setHeightVariation(0.5F).setTemperature(0.2F).setRainfall(0.3F)));
        registerBiome(132, "mutated_forest", new BiomeForest(net.minecraft.world.biome.BiomeForest.Type.FLOWER, (new BiomeProperties("Flower Forest")).setBaseBiome("forest").setHeightVariation(0.4F).setTemperature(0.7F).setRainfall(0.8F)));
        registerBiome(133, "mutated_taiga", new BiomeTaiga(net.minecraft.world.biome.BiomeTaiga.Type.NORMAL, (new BiomeProperties("Taiga M")).setBaseBiome("taiga").setBaseHeight(0.3F).setHeightVariation(0.4F).setTemperature(0.25F).setRainfall(0.8F)));
        registerBiome(134, "mutated_swampland", new BiomeSwamp((new BiomeProperties("Swampland M")).setBaseBiome("swampland").setBaseHeight(-0.1F).setHeightVariation(0.3F).setTemperature(0.8F).setRainfall(0.9F).setWaterColor(14745518)));
        registerBiome(140, "mutated_ice_flats", new BiomeSnow(true, (new BiomeProperties("Ice Plains Spikes")).setBaseBiome("ice_flats").setBaseHeight(0.425F).setHeightVariation(0.45000002F).setTemperature(0.0F).setRainfall(0.5F).setSnowEnabled()));
        registerBiome(149, "mutated_jungle", new BiomeJungle(false, (new BiomeProperties("Jungle M")).setBaseBiome("jungle").setBaseHeight(0.2F).setHeightVariation(0.4F).setTemperature(0.95F).setRainfall(0.9F)));
        registerBiome(151, "mutated_jungle_edge", new BiomeJungle(true, (new BiomeProperties("JungleEdge M")).setBaseBiome("jungle_edge").setBaseHeight(0.2F).setHeightVariation(0.4F).setTemperature(0.95F).setRainfall(0.8F)));
        registerBiome(155, "mutated_birch_forest", new BiomeForestMutated((new BiomeProperties("Birch Forest M")).setBaseBiome("birch_forest").setBaseHeight(0.2F).setHeightVariation(0.4F).setTemperature(0.6F).setRainfall(0.6F)));
        registerBiome(156, "mutated_birch_forest_hills", new BiomeForestMutated((new BiomeProperties("Birch Forest Hills M")).setBaseBiome("birch_forest_hills").setBaseHeight(0.55F).setHeightVariation(0.5F).setTemperature(0.6F).setRainfall(0.6F)));
        registerBiome(157, "mutated_roofed_forest", new BiomeForest(net.minecraft.world.biome.BiomeForest.Type.ROOFED, (new BiomeProperties("Roofed Forest M")).setBaseBiome("roofed_forest").setBaseHeight(0.2F).setHeightVariation(0.4F).setTemperature(0.7F).setRainfall(0.8F)));
        registerBiome(158, "mutated_taiga_cold", new BiomeTaiga(net.minecraft.world.biome.BiomeTaiga.Type.NORMAL, (new BiomeProperties("Cold Taiga M")).setBaseBiome("taiga_cold").setBaseHeight(0.3F).setHeightVariation(0.4F).setTemperature(-0.5F).setRainfall(0.4F).setSnowEnabled()));
        registerBiome(160, "mutated_redwood_taiga", new BiomeTaiga(net.minecraft.world.biome.BiomeTaiga.Type.MEGA_SPRUCE, (new BiomeProperties("Mega Spruce Taiga")).setBaseBiome("redwood_taiga").setBaseHeight(0.2F).setHeightVariation(0.2F).setTemperature(0.25F).setRainfall(0.8F)));
        registerBiome(161, "mutated_redwood_taiga_hills", new BiomeTaiga(net.minecraft.world.biome.BiomeTaiga.Type.MEGA_SPRUCE, (new BiomeProperties("Redwood Taiga Hills M")).setBaseBiome("redwood_taiga_hills").setBaseHeight(0.2F).setHeightVariation(0.2F).setTemperature(0.25F).setRainfall(0.8F)));
        registerBiome(162, "mutated_extreme_hills_with_trees", new BiomeHills(Type.MUTATED, (new BiomeProperties("Extreme Hills+ M")).setBaseBiome("extreme_hills_with_trees").setBaseHeight(1.0F).setHeightVariation(0.5F).setTemperature(0.2F).setRainfall(0.3F)));
        registerBiome(163, "mutated_savanna", new BiomeSavannaMutated((new BiomeProperties("Savanna M")).setBaseBiome("savanna").setBaseHeight(0.3625F).setHeightVariation(1.225F).setTemperature(1.1F).setRainfall(0.0F).setRainDisabled()));
        registerBiome(164, "mutated_savanna_rock", new BiomeSavannaMutated((new BiomeProperties("Savanna Plateau M")).setBaseBiome("savanna_rock").setBaseHeight(1.05F).setHeightVariation(1.2125001F).setTemperature(1.0F).setRainfall(0.0F).setRainDisabled()));
        registerBiome(165, "mutated_mesa", new BiomeMesa(true, false, (new BiomeProperties("Mesa (Bryce)")).setBaseBiome("mesa").setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
        registerBiome(166, "mutated_mesa_rock", new BiomeMesa(false, true, (new BiomeProperties("Mesa Plateau F M")).setBaseBiome("mesa_rock").setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
        registerBiome(167, "mutated_mesa_clear_rock", new BiomeMesa(false, false, (new BiomeProperties("Mesa Plateau M")).setBaseBiome("mesa_clear_rock").setBaseHeight(0.45F).setHeightVariation(0.3F).setTemperature(2.0F).setRainfall(0.0F).setRainDisabled()));
    }


    @Keep
    public static void registerBiome(int id, String name, Biome b) {
        REGISTRY.register(id, new ResourceLocation(name), b);
        if (b.isMutation()) {
            MUTATION_TO_BASE_ID_MAP.put(b, getIdForBiome((Biome)REGISTRY.getObject(new ResourceLocation(b.baseBiomeRegName))));
        }
    }


    static {
        STONE = Blocks.STONE.getDefaultState();
        AIR = Blocks.AIR.getDefaultState();
        BEDROCK = Blocks.BEDROCK.getDefaultState();
        GRAVEL = Blocks.GRAVEL.getDefaultState();
        RED_SANDSTONE = Blocks.RED_SANDSTONE.getDefaultState();
        SANDSTONE = Blocks.SANDSTONE.getDefaultState();
        ICE = Blocks.ICE.getDefaultState();
        WATER = Blocks.WATER.getDefaultState();
        MUTATION_TO_BASE_ID_MAP = new ObjectIntIdentityMap();
        TEMPERATURE_NOISE = new NoiseGeneratorPerlin(new Random(1234L), 1);
        GRASS_COLOR_NOISE = new NoiseGeneratorPerlin(new Random(2345L), 1);
        DOUBLE_PLANT_GENERATOR = new WorldGenDoublePlant();
        TREE_FEATURE = new WorldGenTrees(false);
        BIG_TREE_FEATURE = new WorldGenBigTree(false);
        SWAMP_FEATURE = new WorldGenSwamp();
        REGISTRY = GameData.getWrapper(Biome.class);
    }


    @Keep
    public static enum TempCategory {
        OCEAN,
        COLD,
        MEDIUM,
        WARM;

        private TempCategory() {}
    }


    @Keep
    public static class SpawnListEntry extends WeightedRandom.Item {
        public Class<? extends EntityLiving> entityClass;
        public int minGroupCount;
        public int maxGroupCount;

        public SpawnListEntry(Class<? extends EntityLiving> clazz, int p_i1970_2_, int p_i1970_3_, int p_i1970_4_) {
            super(p_i1970_2_);
            this.entityClass = clazz;
            this.minGroupCount = p_i1970_3_;
            this.maxGroupCount = p_i1970_4_;
        }


        @Override
        public String toString() {
            return this.entityClass.getSimpleName() + "*(" + this.minGroupCount + "-" + this.maxGroupCount + "):" + this.itemWeight;
        }


        public EntityLiving newInstance(World world) throws Exception {
            EntityEntry entry = EntityRegistry.getEntry(this.entityClass);
            return entry != null ? (EntityLiving)entry.newInstance(world) : this.entityClass.getConstructor(World.class).newInstance(world);
        }
    }


    @Keep
    public static class BiomeProperties {
        private final String biomeName;
        private float baseHeight = 0.1F;
        private float heightVariation = 0.2F;
        private float temperature = 0.5F;
        private float rainfall = 0.5F;
        private int waterColor = 16777215;
        private boolean enableSnow;
        private boolean enableRain = true;
        @Nullable
        private String baseBiomeRegName;

        public BiomeProperties(String name) {
            this.biomeName = name;
        }

        public BiomeProperties setTemperature(float temperature) {
            if (temperature > 0.1F && temperature < 0.2F) {
                throw new IllegalArgumentException("Please avoid temperatures in the range 0.1 - 0.2 because of snow");
            } else {
                this.temperature = temperature;
                return this;
            }
        }


        public BiomeProperties setRainfall(float rainfall) {
            this.rainfall = rainfall;
            return this;
        }


        public BiomeProperties setBaseHeight(float height) {
            this.baseHeight = height;
            return this;
        }


        public BiomeProperties setHeightVariation(float var) {
            this.heightVariation = var;
            return this;
        }


        public BiomeProperties setRainDisabled() {
            this.enableRain = false;
            return this;
        }


        public BiomeProperties setSnowEnabled() {
            this.enableSnow = true;
            return this;
        }


        public BiomeProperties setWaterColor(int color) {
            this.waterColor = color;
            return this;
        }


        public BiomeProperties setBaseBiome(String name) {
            this.baseBiomeRegName = name;
            return this;
        }
    }


    @Keep
    public static class FlowerEntry extends WeightedRandom.Item {
        @Keep
        public final IBlockState state;

        public FlowerEntry(IBlockState state, int weight) {
            super(weight);
            this.state = state;
        }
    }
}
