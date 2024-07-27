package plus.space.server.gen.v2;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.*;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import plus.fserver.ElcaConfig;
import plus.fserver.api.ElcaWorldServer;
import javax.annotation.Nullable;
import java.util.Random;


public class PAsyncOverworld implements CChunkGenerator.Populator{
    private final Random rand;
    private final World world;
    private final boolean mapFeaturesEnabled;

    private ChunkGeneratorSettings settings;
    private MapGenStronghold strongholdGenerator = new MapGenStronghold();
    private MapGenVillage villageGenerator = new MapGenVillage();
    private MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
    private MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();
    private StructureOceanMonument oceanMonumentGenerator = new StructureOceanMonument();
    private WoodlandMansionMapGen woodlandMansionGenerator = null;
    private final IChunkGenerator parent;


    public PAsyncOverworld(Random rand, IChunkGenerator parent, GAsyncOverworld gen, World worldIn, boolean mapFeaturesEnabledIn, String generatorOptions) {
        if(gen==null) {
            if(mapFeaturesEnabledIn) {
                strongholdGenerator = (MapGenStronghold) net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(strongholdGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.STRONGHOLD);
                villageGenerator = (MapGenVillage) net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(villageGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.VILLAGE);
                mineshaftGenerator = (MapGenMineshaft) net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(mineshaftGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.MINESHAFT);
                scatteredFeatureGenerator = (MapGenScatteredFeature) net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(scatteredFeatureGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.SCATTERED_FEATURE);

                oceanMonumentGenerator = (StructureOceanMonument) net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(oceanMonumentGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.OCEAN_MONUMENT);
                woodlandMansionGenerator = (WoodlandMansionMapGen) TerrainGen.getModdedMapGen(woodlandMansionGenerator, InitMapGenEvent.EventType.WOODLAND_MANSION);
            }
        } else {
            strongholdGenerator = gen.strongholdGenerator;
            villageGenerator    = gen.villageGenerator;
            mineshaftGenerator  = gen.mineshaftGenerator;
            scatteredFeatureGenerator = gen.scatteredFeatureGenerator;
            oceanMonumentGenerator    = gen.oceanMonumentGenerator;
            woodlandMansionGenerator  = gen.woodlandMansionGenerator;
        }
        this.parent = parent;
        this.world = worldIn;
        this.mapFeaturesEnabled = mapFeaturesEnabledIn;
        this.rand = rand;


        if (generatorOptions != null) {
            this.settings = ChunkGeneratorSettings.Factory.jsonToFactory(generatorOptions).build();
            worldIn.setSeaLevel(this.settings.seaLevel);
        }
    }


    @Override
    public void populate(int x, int z) {
        BlockFalling.fallInstantly = true;
        int i = x * 16;
        int j = z * 16;
        BlockPos blockpos = new BlockPos(i, 0, j);
        Biome biome = this.world.getBiome(blockpos.add(16, 0, 16));
        this.rand.setSeed(this.world.getSeed());
        long k = this.rand.nextLong() / 2L * 2L + 1L;
        long l = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed((long)x * k + (long)z * l ^ this.world.getSeed());

        ChunkPos chunkpos = new ChunkPos(x, z);

        ForgeEventFactory.onChunkPopulate(true, parent, this.world, this.rand, x, z, false);

        if (biome != Biomes.DESERT && biome != Biomes.DESERT_HILLS && this.settings.useWaterLakes && this.rand.nextInt(this.settings.waterLakeChance) == 0)
            if (TerrainGen.populate(parent, this.world, this.rand, x, z, false, PopulateChunkEvent.Populate.EventType.LAKE)) {
                int i1 = this.rand.nextInt(16) + 8;
                int j1 = this.rand.nextInt(256);
                int k1 = this.rand.nextInt(16) + 8;
                (new WorldGenLakes(Blocks.WATER)).generate(this.world, this.rand, blockpos.add(i1, j1, k1));
            }

        if (this.rand.nextInt(this.settings.lavaLakeChance / 10) == 0 && this.settings.useLavaLakes)
            if (TerrainGen.populate(parent, this.world, this.rand, x, z, false, PopulateChunkEvent.Populate.EventType.LAVA)) {
                int i2 = this.rand.nextInt(16) + 8;
                int l2 = this.rand.nextInt(this.rand.nextInt(248) + 8);
                int k3 = this.rand.nextInt(16) + 8;

                if (l2 < this.world.getSeaLevel() || this.rand.nextInt(this.settings.lavaLakeChance / 8) == 0)
                {
                    (new WorldGenLakes(Blocks.LAVA)).generate(this.world, this.rand, blockpos.add(i2, l2, k3));
                }
            }

        if (this.settings.useDungeons)
            if (TerrainGen.populate(parent, this.world, this.rand, x, z, false, PopulateChunkEvent.Populate.EventType.DUNGEON))
            {
                for (int j2 = 0; j2 < this.settings.dungeonChance; ++j2) {
                    int i3 = this.rand.nextInt(16) + 8;
                    int l3 = this.rand.nextInt(256);
                    int l1 = this.rand.nextInt(16) + 8;
                    (new WorldGenDungeons()).generate(this.world, this.rand, blockpos.add(i3, l3, l1));
                }
            }

        biome.decorate(this.world, this.rand, new BlockPos(i, 0, j));


        Biome finalBiome = biome;
        ((ElcaWorldServer)world).addSyncTask(() -> {
            boolean flag0 = false;
            if (mapFeaturesEnabled) {
                if (settings.useMineShafts) {
                    mineshaftGenerator.generateStructure(world, rand, chunkpos);
                }
                if (settings.useVillages) {
                    flag0 = villageGenerator.generateStructure(world, rand, chunkpos);
                }
                if (settings.useStrongholds) {
                    strongholdGenerator.generateStructure(world, rand, chunkpos);
                }
                if (settings.useTemples) {
                    scatteredFeatureGenerator.generateStructure(world, rand, chunkpos);
                }
                if (settings.useMonuments) {
                    oceanMonumentGenerator.generateStructure(world, rand, chunkpos);
                }
                if (settings.useMansions) {
                    woodlandMansionGenerator.generateStructure(world, rand, chunkpos);
                }
            }
            if (TerrainGen.populate( parent, world, rand, x, z, flag0, PopulateChunkEvent.Populate.EventType.ANIMALS))
                WorldEntitySpawner.performWorldGenSpawning(world, finalBiome, i + 8, j + 8, 16, 16, rand);
        });

        //blockpos = blockpos.add(8, 0, 8);

        if (TerrainGen.populate(parent, this.world, this.rand, x, z, false, PopulateChunkEvent.Populate.EventType.ICE)) {
            //todo fix snow
            Chunk chunk = this.world.getChunk(x, z);
            BlockPos down;
            for (int curX = 0; curX < 16; ++curX) {
                for (int curZ = 0; curZ < 16; ++curZ) {
                    blockpos = chunk.getPrecipitationHeight(curX, curZ);
                    if(blockpos.getY() < 0 || blockpos.getY() > 255)continue;

                    biome = chunk.getBiome(blockpos, world.getBiomeProvider());

                    if(biome.getTemperature(blockpos) < 0.155F){
                        if (canBlockFreezeBody(chunk, down = blockpos.down())) {
                            chunk.setBlockState(down, Blocks.ICE.getDefaultState());
                        }
                        if (canSnowAt(chunk, blockpos, down)) {
                            chunk.setBlockStateFastNoTile(
                                    blockpos.getX(), blockpos.getY(), blockpos.getZ(),
                                    Blocks.SNOW_LAYER.getDefaultState()
                            );
                        }
                    }
                }
            }
        }//Forge: End ICE

        ForgeEventFactory.onChunkPopulate(false, parent, this.world, this.rand, x, z, false);

        BlockFalling.fallInstantly = false;
    }


    private boolean canSnowAt(Chunk chunk, BlockPos pos, BlockPos down) {
        if (chunk.getLightFor(EnumSkyBlock.BLOCK, pos) < 10) {
            IBlockState state = chunk.getBlockState(pos);
            return state.getBlock().isAir(state, this.world, pos)
                    && Blocks.SNOW_LAYER.canPlaceBlockAt(this.world, down);
        }
        return false;
    }


    private boolean canBlockFreezeBody(Chunk chunk, BlockPos pos) {
        if (chunk.getLightFor(EnumSkyBlock.BLOCK, pos) < 10) {
            IBlockState state = chunk.getBlockState(pos);
            Block block = state.getBlock();
            return (block == Blocks.WATER || block == Blocks.FLOWING_WATER) && state.getValue(BlockLiquid.LEVEL) == 0;
        }
        return false;
    }


    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        boolean flag = false;
        if (this.settings.useMonuments && this.mapFeaturesEnabled && chunkIn.getInhabitedTime() < 3600L) {
            flag |= this.oceanMonumentGenerator.generateStructure(this.world, this.rand, new ChunkPos(x, z));
        }
        return flag;
    }


    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        if (!this.mapFeaturesEnabled) {
            return false;
        } else if ("Stronghold".equals(structureName) && this.strongholdGenerator != null) {
            return this.strongholdGenerator.isInsideStructure(pos);
        }
        else if ("Mansion".equals(structureName) && this.woodlandMansionGenerator != null) {
            return this.woodlandMansionGenerator.isInsideStructure(pos);
        } else if ("Monument".equals(structureName) && this.oceanMonumentGenerator != null) {
            return this.oceanMonumentGenerator.isInsideStructure(pos);
        } else if ("Village".equals(structureName) && this.villageGenerator != null) {
            return this.villageGenerator.isInsideStructure(pos);
        } else if ("Mineshaft".equals(structureName) && this.mineshaftGenerator != null) {
            return this.mineshaftGenerator.isInsideStructure(pos);
        } else {
            return "Temple".equals(structureName) && this.scatteredFeatureGenerator != null ? this.scatteredFeatureGenerator.isInsideStructure(pos) : false;
        }
    }


    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
        if (!this.mapFeaturesEnabled) {
            return null;
        } else if ("Stronghold".equals(structureName) && this.strongholdGenerator != null) {
            return this.strongholdGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
        }
        else if ("Mansion".equals(structureName) && this.woodlandMansionGenerator != null) {
            return this.woodlandMansionGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
        }
        else if ("Monument".equals(structureName) && this.oceanMonumentGenerator != null) {
            return this.oceanMonumentGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
        }
        else if ("Village".equals(structureName) && this.villageGenerator != null) {
            return this.villageGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
        }
        else if ("Mineshaft".equals(structureName) && this.mineshaftGenerator != null) {
            return this.mineshaftGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
        } else {
            return "Temple".equals(structureName) && this.scatteredFeatureGenerator != null ? this.scatteredFeatureGenerator.getNearestStructurePos(worldIn, position, findUnexplored) : null;
        }
    }


    @Override
    public boolean maybeAsync() {
        return ElcaConfig.OVERWORLD_ASYNC_POPULATION;
    }
}
