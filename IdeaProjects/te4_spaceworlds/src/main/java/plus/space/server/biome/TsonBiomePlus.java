package plus.space.server.biome;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSand;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import plus.fserver.api.ExBiome;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.world.generator.struct.*;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.Random;


public class TsonBiomePlus extends TsonBiomeServer implements ExBiome {
    private final ChunkGeneratorSettings settings;
    private final int seaLevel;
    private final IWorldStruct[] structures;
    private final IWorldStruct[] structuresOnPop;
    private final IBlockState oceanBlock;

    public TsonBiomePlus(TsonMap map) {
        super(map);
        settings = getSettings(map.get("generator"));
        seaLevel = settings==null?0:settings.seaLevel;

        IWorldStruct[][] structures0 = build(map.get("structures"));
        structures      = structures0[0];
        structuresOnPop = structures0[1];

        if(map.containsKey("ocean_block")){
            oceanBlock = blockOfTson(map.get("ocean_block"));
        } else {
            oceanBlock = Blocks.WATER.getDefaultState();
        }
    }

    protected static ChunkGeneratorSettings getSettings(TsonObj map0){
        if(map0==null || !map0.isMap())return null;
        TsonMap map = map0.getMap();
        if(map==null)return null;
        ChunkGeneratorSettings.Factory def0 = new ChunkGeneratorSettings.Factory();
        def0.setDefaults();
        ChunkGeneratorSettings def = def0.build();
        ChunkGeneratorSettings.Factory nw = new ChunkGeneratorSettings.Factory();
        map.ifContainsMap("base",(stn)-> {
            nw.baseSize = stn.getOrDefaultFloat("base_size", def.baseSize);
            nw.seaLevel = getParam(stn, "sea_lvl",0);
            nw.useCaves = stn.getOrDefaultBool("caves", def.useCaves);
            nw.useDungeons = stn.getOrDefaultBool("dungeons", def.useDungeons);
            nw.dungeonChance = getParam(stn, "dungeons_chance", def.dungeonChance);
            nw.useStrongholds = stn.getOrDefaultBool("strongholds", def.useStrongholds);
            nw.useTemples = stn.getOrDefaultBool("temples",def.useTemples);
            nw.useWaterLakes = stn.getOrDefaultBool("water_lakes", def.useLavaLakes);
            nw.waterLakeChance = getParam(stn, "water_lake_chance", def.waterLakeChance);
            nw.useLavaLakes = stn.getOrDefaultBool("lava_likes", def.useLavaLakes);
            nw.lavaLakeChance = getParam(stn, "lava_lake_chance", def.lavaLakeChance);
            nw.useLavaOceans = stn.getOrDefaultBool("lava_sea", def.useLavaOceans);
            nw.riverSize = stn.getOrDefaultBool("rivers", true)?1:0;
        });
        map.ifContainsMap("scale",(stn)->{
            nw.heightScale     = stn.getOrDefaultFloat("height",def.heightScale);
            nw.biomeScaleWeight = stn.getOrDefaultFloat("biome_weight", def.biomeScaleWeight);
            nw.coordinateScale = stn.getOrDefaultFloat("cord", def.coordinateScale);
            nw.mainNoiseScaleX = stn.getOrDefaultFloat("noise_x", def.mainNoiseScaleX);
            nw.mainNoiseScaleY = stn.getOrDefaultFloat("noise_y", def.mainNoiseScaleY);
            nw.mainNoiseScaleZ = stn.getOrDefaultFloat("noise_z", def.mainNoiseScaleZ);
            nw.depthNoiseScaleX = stn.getOrDefaultFloat("depth_noise_x", def.depthNoiseScaleX);
            nw.depthNoiseScaleZ = stn.getOrDefaultFloat("depth_noise_z", def.depthNoiseScaleZ);
        });
        map.ifContainsMap("ore",(ore)->{
            ore.ifContainsMap("dirt",(val)->{
                nw.dirtCount = getParam(val, "count", def.dirtCount);
                nw.dirtSize = getParam(val, "size", def.dirtSize);
                nw.dirtMaxHeight = getParam(val, "max_height",def.dirtMaxHeight);
                nw.dirtMinHeight = getParam(val, "min_height",def.dirtMinHeight);
            });
            ore.ifContainsMap("diorite",(val)->{
                nw.dioriteCount = getParam(val, "count", def.dioriteCount);
                nw.dioriteSize = getParam(val, "size", def.dioriteSize);
                nw.dioriteMaxHeight = getParam(val, "max_height",def.dioriteMaxHeight);
                nw.dioriteMinHeight = getParam(val, "min_height",def.dioriteMinHeight);
            });
            ore.ifContainsMap("andesite",(val)->{
                nw.andesiteCount = getParam(val, "count", def.andesiteCount);
                nw.andesiteSize = getParam(val, "size", def.andesiteSize);
                nw.andesiteMaxHeight = getParam(val, "max_height",def.andesiteMaxHeight);
                nw.andesiteMinHeight = getParam(val, "min_height",def.andesiteMinHeight);
            });
            ore.ifContainsMap("granite",(val)->{
                nw.graniteCount = getParam(val, "count", def.graniteCount);
                nw.graniteSize = getParam(val, "size", def.graniteSize);
                nw.graniteMaxHeight = getParam(val, "max_height",def.graniteMaxHeight);
                nw.graniteMinHeight = getParam(val, "min_height",def.graniteMinHeight);
            });
            ore.ifContainsMap("gravel",(val)->{
                nw.gravelCount = getParam(val, "count", def.gravelCount);
                nw.gravelSize = getParam(val, "size", def.gravelSize);
                nw.gravelMaxHeight = getParam(val, "max_height",def.gravelMaxHeight);
                nw.gravelMinHeight = getParam(val, "min_height",def.gravelMinHeight);
            });
            ore.ifContainsMap("coal",(val)->{
                nw.coalCount = getParam(val, "count", def.coalCount);
                nw.coalSize = getParam(val, "size", def.coalSize);
                nw.coalMaxHeight = getParam(val, "max_height",def.coalMaxHeight);
                nw.coalMinHeight = getParam(val, "min_height",def.coalMinHeight);
            });
            ore.ifContainsMap("iron",(val)->{
                nw.ironCount = getParam(val, "count", def.ironCount);
                nw.ironSize = getParam(val, "size", def.ironSize);
                nw.ironMaxHeight = getParam(val, "max_height",def.ironMaxHeight);
                nw.ironMinHeight = getParam(val, "min_height",def.ironMinHeight);
            });
            ore.ifContainsMap("lapis",(val)->{
                nw.lapisSpread = getParam(val, "spread", def.lapisSpread);
                nw.lapisSize = getParam(val, "size", def.lapisSize);
                nw.lapisCount = getParam(val, "max_height",def.lapisCount);
                nw.lapisCenterHeight = getParam(val, "min_height",def.lapisCenterHeight);
            });
            ore.ifContainsMap("diamond",(val)->{
                nw.diamondCount = getParam(val, "count", def.diamondCount);
                nw.diamondSize = getParam(val, "size", def.diamondSize);
                nw.diamondMaxHeight = getParam(val, "max_height",def.diamondMaxHeight);
                nw.diamondMinHeight = getParam(val, "min_height",def.diamondMinHeight);
            });
            ore.ifContainsMap("redstone",(val)->{
                nw.redstoneCount = getParam(val, "count", def.redstoneCount);
                nw.redstoneSize = getParam(val, "size", def.redstoneSize);
                nw.redstoneMaxHeight = getParam(val, "max_height",def.redstoneMaxHeight);
                nw.redstoneMinHeight = getParam(val, "min_height",def.redstoneMaxHeight);
            });
            ore.ifContainsMap("gold",(val)->{
                nw.goldCount = getParam(val, "count", def.goldCount);
                nw.goldSize = getParam(val, "size", def.goldSize);
                nw.goldMaxHeight = getParam(val, "max_height",def.goldMaxHeight);
                nw.goldMinHeight = getParam(val, "min_height",def.goldMinHeight);
            });
        });
        return nw.build();
    }


    public IBlockState getOceanBlock() {
        return oceanBlock;
    }


    public int getSeaLevel() {
        return seaLevel;
    }


    private static IWorldStruct[][] build(TsonObj obj){
        if(obj==null)return new IWorldStruct[][]{null, null};
        TsonList list = obj.getList();
        int sizeOnGen = 0, sizeOnPop = 0;
        for(int i = 0; i < list.size(); i++) {
            TsonMap struct = list.getMap(i);
            boolean onPopulate = struct.getOrDefaultBool("on-populate", true);
            if(onPopulate){
                sizeOnPop += 1;
            } else {
                sizeOnGen += 1;
            }
        }
        IWorldStruct[][] structures0 = new IWorldStruct[][]{
                sizeOnGen == 0?null:new IWorldStruct[sizeOnGen],
                sizeOnPop == 0?null:new IWorldStruct[sizeOnPop]
        };

        for(int i = 0; i < list.size(); i++){
            TsonMap struct = list.getMap(i);
            boolean onPopulate = struct.getOrDefaultBool("on-populate", true);
            IWorldStruct[] structures = structures0[onPopulate?1:0];

            if(struct.containsKey("add")){
                if(struct.containsKey("weights")){
                    structures[i] = new WorldOneOfSeveralStruct(struct);
                } else {
                    structures[i] = new WorldMultiStructure(struct);
                }
            } else {
                structures[i] = new WorldStructure(struct);
            }
            if(struct.containsKey("sub")){
                TsonList listSub = struct.getList("sub");
                for (TsonObj sub:listSub){
                    Structure.build(sub.getMap());
                }
            }
        }
        return structures0;
    }


    public ChunkGeneratorSettings getSelfSettings() {
        return settings;
    }


    public IWorldStruct[] getStructures() {
        return structures;
    }


    public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
        this.generateBiomeTerrainPatch(worldIn, rand, chunkPrimerIn, x, z, noiseVal, this.topBlock, this.fillerBlock);
    }


    @Override
    public void decorate(World world, Random rand, BlockPos pos) {
        super.decorate(world, rand, pos);
        if(structuresOnPop == null){
//            if(seaLevel != 0){
//                fixLiquid(new ChunkSelector(world), world, rand, pos);
//            }
            return;
        }

        ChunkSelector selector = new ChunkSelector(world);
//        if(seaLevel != 0){
//            fixLiquid(selector, world, rand, pos);
//        }

        for (IWorldStruct struct:structuresOnPop){
            float sp = struct.getSpawnChance();
            while (sp > 0) {
                if(rand.nextFloat() < sp) {
                    int x = pos.getX() + (rand.nextInt() & 15);
                    int z = pos.getZ() + (rand.nextInt() & 15);

                    int y = selector.getChunk(pos).getPrecipitationHeightY(x, z);
                    if(y < 0)return;
                    IBlockState state = selector.getStateUnchecked(x, y, z);
                    Material mat = state.getMaterial();
                    while (mat == Material.LEAVES || mat.isReplaceable()){
                        state = selector.getStateUnchecked(x, --y, z);
                        mat = state.getMaterial();
                    }
                    if (struct.canPlaceOn(state.getBlock())) {
                        struct.generate(world, x, y, z);
                    }
                }
                sp -= 1;
            }
        }
    }


//    public void fixLiquid(ChunkSelector selector, World world, Random rand, BlockPos pos){
//        Chunk chunk = selector.getChunk(pos.getX(), pos.getZ());
//        BlockPos.MutableBlockPos mut  = null;
//        int y = world.getSeaLevel() + seaLevel;
//        int delta = y > world.getSeaLevel()? world.getSeaLevel() - seaLevel:y-2;
//        if(y < 0)y = 0;
//
//        for (int x = 0; x < 16; x++) {
//            for (int z = 0; z < 16; z++) {
//                int dy = y;
//                IBlockState state = chunk.getBlockStateUnchecked(x, dy, z);
//                while (dy > delta && state == BlockIDIndexes.AIR_STATE){
//                    state = chunk.getBlockStateUnchecked(x, --dy, z);
//                }
//                if(state.getMaterial().isLiquid()){
//                    if(mut == null)mut = new BlockPos.MutableBlockPos(x, dy, z);
//                    else mut.setPosB(x, dy, z);
//                    ((BlockLiquid) state).neighborChanged(state, world, mut, state.getBlock(), mut);
//                }
//            }
//        }
//    }


    @Override
    public BiomeDecorator createBiomeDecorator() {
        return new BiomeDecorator();
    }


    public void generateBiomeTerrainPatch(World worldIn, Random rand, ChunkPrimer primer, int x, int z,
                                          double noiseVal, final IBlockState top0, final IBlockState filler0) {
        int seaLvl = worldIn.getSeaLevel() + seaLevel;

        int j = -1;
        int k = (int)(noiseVal / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
        int l = x & 15;
        int i1 = z & 15;
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        IBlockState top = top0, filler = filler0;

        for (int j1 = 255; j1 >= 0; --j1) {
            if (j1 <= rand.nextInt(5)) {
                primer.setBlockState(i1, j1, l, BEDROCK);
            } else {
                IBlockState iblockstate2 = primer.getBlockState(i1, j1, l);

                if (iblockstate2.getMaterial() == Material.AIR) {
                    j = -1;
                } else if (iblockstate2.getBlock() == Blocks.STONE) {
                    if (j == -1) {
                        if (k <= 0) {
                            top = AIR;
                            filler = STONE;
                        } else if (j1 >= seaLvl - 4 && j1 <= seaLvl + 1) {
                            top = top0;
                            filler = filler0;
                        }

                        if (j1 < seaLvl && (top == null || top.getMaterial() == Material.AIR)) {
                            if (this.getTemperature(blockPos.setPos(x, j1, z)) < 0.15F) {
                                top = ICE;
                            } else {
                                top = WATER;
                            }
                        }

                        j = k;

                        if (j1 >= seaLvl - 1) {
                            primer.setBlockState(i1, j1, l, top);
                        } else if (j1 < seaLvl - 7 - k) {
                            top = AIR;
                            filler = STONE;
                            primer.setBlockState(i1, j1, l, GRAVEL);
                        } else {
                            primer.setBlockState(i1, j1, l, filler);
                        }
                    } else if (j > 0) {
                        --j;
                        primer.setBlockState(i1, j1, l, filler);

                        if (j == 0 && filler.getBlock() == Blocks.SAND && k > 1) {
                            j = rand.nextInt(4) + Math.max(0, j1 - 63);
                            filler = filler.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND ? RED_SANDSTONE : SANDSTONE;
                        }
                    }
                }
            }
        }
    }
}