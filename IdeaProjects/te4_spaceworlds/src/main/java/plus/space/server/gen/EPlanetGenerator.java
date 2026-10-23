package plus.space.server.gen;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import plus.fserver.world.generator.struct.IWorldStruct;
import plus.fserver.world.generator.vr.MultiChunkCache;
import plus.space.server.biome.TsonBiomePlus;
import plus.space.server.SolarWorldItem;


@Deprecated
public class EPlanetGenerator extends PubChunkGeneratorOverworld {
    //private final WorldStructure[] structurs = new WorldStructure[]{};
    private final MultiChunkCache cache = new MultiChunkCache();
    public EPlanetGenerator(WorldServer world, SolarWorldItem item){
        super(
                world,
                world.getSeed(),
                world.getWorldInfo().isMapFeaturesEnabled(),
                world.getWorldInfo().getGeneratorOptions()
        );
    }


    private int[] waterLvl(Biome[] biomesForGeneration0) {
        int[] cache = new int[256];
        Biome biome = null;
        int prev = -1;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                Biome biome2 = biomesForGeneration0[(x << 4) + z];
                if (biome2 == biome) {
                    cache[x * 16 + z] = prev;
                } else {
                    biome = biome2;
                    if (biome2 instanceof TsonBiomePlus) {
                        prev = cache[x * 16 + z] = settings.seaLevel + ((TsonBiomePlus) biome).getSeaLevel();
                    } else {
                        prev = cache[x * 16 + z] = settings.seaLevel;
                    }
                }
            }
        }
        return cache;
    }


    public void setBlocksInChunk(int[] lvl, IBlockState oceanBlock, int x, int z, ChunkPrimer primer) {
        this.biomesForGeneration = this.world.getBiomeProvider().getBiomesForGeneration(this.biomesForGeneration, x * 4 - 2, z * 4 - 2, 10, 10);
        this.generateHeightmap(x * 4, 0, z * 4);

        for (int i = 0; i < 4; ++i) {
            int j = i * 5;
            int k = (i + 1) * 5;

            for (int l = 0; l < 4; ++l) {
                int i1 = (j + l) * 33;
                int j1 = (j + l + 1) * 33;
                int k1 = (k + l) * 33;
                int l1 = (k + l + 1) * 33;

                for (int i2 = 0; i2 < 32; ++i2) {
                    double d1 = this.heightMap[i1 + i2];
                    double d2 = this.heightMap[j1 + i2];
                    double d3 = this.heightMap[k1 + i2];
                    double d4 = this.heightMap[l1 + i2];
                    double d5 = (this.heightMap[i1 + i2 + 1] - d1) * 0.125D;
                    double d6 = (this.heightMap[j1 + i2 + 1] - d2) * 0.125D;
                    double d7 = (this.heightMap[k1 + i2 + 1] - d3) * 0.125D;
                    double d8 = (this.heightMap[l1 + i2 + 1] - d4) * 0.125D;

                    for (int j2 = 0; j2 < 8; ++j2) {
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * 0.25D;
                        double d13 = (d4 - d2) * 0.25D;

                        int ay = i2 * 8 + j2;

                        for (int k2 = 0; k2 < 4; ++k2) {
                            double d16 = (d11 - d10) * 0.25D;
                            double lvt_45_1_ = d10 - d16;

                            int ax = i * 4 + k2;
                            for (int l2 = 0; l2 < 4; ++l2) {
                                int az = l * 4 + l2;

                                if ((lvt_45_1_ += d16) > 0.0D) {
                                    primer.setBlockState(ax, ay, az, STONE);
                                } else if(ay < lvl[az*16 + ax]){
                                    primer.setBlockState(ax, ay, az, oceanBlock);
                                }
                            }
                            d10 += d12;
                            d11 += d13;
                        }
                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }



//    private static int getTopBlock(ChunkPrimer primer, int x, int z) {
//        int low = 0;
//        int high = 255;
//        while (low <= high) {
//            int mid = (low + high) >>> 1;
//            if (primer.getBlockState(x, mid, z).isFullBlock())
//                low = mid + 1;
//            else high = mid - 1;
//        }
//        return high;
//    }


    public Chunk generateChunk(int x, int z) {
        this.rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);

        ChunkPrimer chunkprimer = cache.primerSafe(world, x, z);

        this.biomesForGeneration = this.world.getBiomeProvider().getBiomes(this.biomesForGeneration, x * 16, z * 16, 16, 16);

        Biome biome = biomesForGeneration[64];

        ChunkGeneratorSettings settings = this.settings;

        if(biome instanceof TsonBiomePlus){
            TsonBiomePlus biome0 = (TsonBiomePlus) biome;
            if(biome0.getSelfSettings() != null) settings = biome0.getSelfSettings();

            this.setBlocksInChunk(waterLvl(biomesForGeneration), biome0.getOceanBlock(), x, z, chunkprimer);
            this.replaceBiomeBlocks(x, z, chunkprimer, this.biomesForGeneration);

            if(biome0.getStructures() != null){
                for(IWorldStruct structure:biome0.getStructures()){
                    float sp = structure.getSpawnChance();
                    while(sp>0){
                        if(rand.nextFloat() < sp){
                            int sX = rand.nextInt(16);
                            int sZ = rand.nextInt(16);
                            int sY = chunkprimer.findGroundBlockIdx(sX, sZ);
                            if(structure.isUnderground()){
                                sY = rand.nextInt(sY);
                                if(sY<3)sY = 3;
                            } else if(!structure.canPlaceOn(chunkprimer.getBlockState(sX, sY, sZ).getBlock()))
                                continue;
                            cache.place(x, z, sX, sY, sZ, structure);
                        }
                        sp -= 1;
                    }
                }
            }
        } else {
            this.setBlocksInChunk(x, z, chunkprimer);
            this.replaceBiomeBlocks(x, z, chunkprimer, this.biomesForGeneration);
        }


        if (settings.useCaves) {
            this.caveGenerator.generate(this.world, x, z, chunkprimer);
        }

        if (settings.useRavines) {
            this.ravineGenerator.generate(this.world, x, z, chunkprimer);
        }

        if (this.mapFeaturesEnabled) {
            if (settings.useMineShafts) {
                this.mineshaftGenerator.generate(this.world, x, z, chunkprimer);
            }

            if (settings.useVillages) {
                this.villageGenerator.generate(this.world, x, z, chunkprimer);
            }

            if (settings.useStrongholds) {
                this.strongholdGenerator.generate(this.world, x, z, chunkprimer);
            }

            if (settings.useTemples) {
                this.scatteredFeatureGenerator.generate(this.world, x, z, chunkprimer);
            }

            if (settings.useMonuments) {
                this.oceanMonumentGenerator.generate(this.world, x, z, chunkprimer);
            }

            if (settings.useMansions) {
                this.woodlandMansionGenerator.generate(this.world, x, z, chunkprimer);
            }
        }

        Chunk chunk = new Chunk(this.world, chunkprimer, x, z);
        byte[] abyte = chunk.getBiomeArray();

        for (int i = 0; i < abyte.length; ++i) {
            abyte[i] = (byte)Biome.getIdForBiome(this.biomesForGeneration[i]);
        }

        chunk.generateSkylightMap();
        cache.clean(x, z);
        return chunk;
    }


//    private void fixWater(int x, int z){
//        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x,0,z);
//        Biome prev = null;
//        for(int ax=0;ax<16;ax++){
//            for(int az=0;az<16;az++){
//                Biome biome = world.getBiome(pos.setPos(ax + x, 0, az + z));
//                if(biome == prev || biome instanceof TsonBiomePlus) {
//                    prev = biome;
//                    int seaLvl = ((TsonBiomePlus) biome).getSeaLevel();
//                    if(seaLvl!=0) {
//                        pos.setY(seaLvl+settings.seaLevel);
//                        IBlockState state = world.getBlockState(pos);
//                        if (state.getBlock() instanceof BlockLiquid) {
//                            //state.getBlock().updateTick(world, pos, state, rand);
//                            world.immediateBlockTick(pos, state, rand);
//                        }
//                    }
//                }
//            }
//        }
//    }


    public void populate(int x, int z) {
        BlockFalling.fallInstantly = true;
        int i = x * 16;
        int j = z * 16;
        BlockPos blockpos = new BlockPos(i, 0, j);
        Biome biome = this.world.getBiome(blockpos.add(16, 0, 16));
        world.rand.setSeed(this.world.getSeed());
        long k = world.rand.nextLong() / 2L * 2L + 1L;
        long l = world.rand.nextLong() / 2L * 2L + 1L;
        world.rand.setSeed((long)x * k + (long)z * l ^ this.world.getSeed());
        boolean flag = false;
        ChunkPos chunkpos = new ChunkPos(x, z);

        net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(true, this, this.world, this.rand, x, z, flag);

        ChunkGeneratorSettings settings = super.settings;
        if(biome instanceof TsonBiomePlus){
            TsonBiomePlus tBiome = ((TsonBiomePlus) biome);
            ChunkGeneratorSettings settings2 = tBiome.getSelfSettings();
            if(settings2 != null)settings = settings2;
        }
        //fixWater(i, j);

        if (this.mapFeaturesEnabled) {
            if (settings.useMineShafts) {
                this.mineshaftGenerator.generateStructure(this.world, this.rand, chunkpos);
            }

            if (settings.useVillages) {
                flag = this.villageGenerator.generateStructure(this.world, this.rand, chunkpos);
            }

            if (settings.useStrongholds) {
                this.strongholdGenerator.generateStructure(this.world, this.rand, chunkpos);
            }

            if (settings.useTemples) {
                this.scatteredFeatureGenerator.generateStructure(this.world, this.rand, chunkpos);
            }

            if (settings.useMonuments) {
                this.oceanMonumentGenerator.generateStructure(this.world, this.rand, chunkpos);
            }

            if (settings.useMansions) {
                this.woodlandMansionGenerator.generateStructure(this.world, this.rand, chunkpos);
            }
        }


        if (biome != Biomes.DESERT && biome != Biomes.DESERT_HILLS && settings.useWaterLakes && !flag && this.rand.nextInt(this.settings.waterLakeChance) == 0) {
            int i1 = this.rand.nextInt(16) + 8;
            int j1 = this.rand.nextInt(256);
            int k1 = this.rand.nextInt(16) + 8;
            (new WorldGenLakes(Blocks.WATER)).generate(this.world, this.rand, blockpos.add(i1, j1, k1));
        }


        if (!flag && this.rand.nextInt(settings.lavaLakeChance / 10) == 0 && settings.useLavaLakes) {
            int i2 = this.rand.nextInt(16) + 8;
            int l2 = this.rand.nextInt(this.rand.nextInt(248) + 8);
            int k3 = this.rand.nextInt(16) + 8;

            if (l2 < this.world.getSeaLevel() || this.rand.nextInt(settings.lavaLakeChance / 8) == 0) {
                (new WorldGenLakes(Blocks.LAVA)).generate(this.world, this.rand, blockpos.add(i2, l2, k3));
            }
        }


        if (this.settings.useDungeons) {
            for (int j2 = 0; j2 < settings.dungeonChance; ++j2) {
                int i3 = this.rand.nextInt(16) + 8;
                int l3 = this.rand.nextInt(256);
                int l1 = this.rand.nextInt(16) + 8;
                (new WorldGenDungeons()).generate(this.world, this.rand, blockpos.add(i3, l3, l1));
            }
        }

        biome.decorate(this.world, this.rand, new BlockPos(i, 0, j));
        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, flag, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ANIMALS))
            WorldEntitySpawner.performWorldGenSpawning(this.world, biome, i + 8, j + 8, 16, 16, this.rand);
        blockpos = blockpos.add(8, 0, 8);

        for (int k2 = 0; k2 < 16; ++k2) {
            for (int j3 = 0; j3 < 16; ++j3) {
                BlockPos blockpos1 = this.world.getPrecipitationHeight(blockpos.add(k2, 0, j3));
                BlockPos blockpos2 = blockpos1.down();

                if (this.world.canBlockFreezeWater(blockpos2)) {
                    this.world.setBlockState(blockpos2, Blocks.ICE.getDefaultState(), 2);
                }

                if (this.world.canSnowAt(blockpos1, true)) {
                    this.world.setBlockState(blockpos1, Blocks.SNOW_LAYER.getDefaultState(), 2);
                }
            }
        }

        net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(false, this, this.world, this.rand, x, z, flag);

        BlockFalling.fallInstantly = false;
    }
}