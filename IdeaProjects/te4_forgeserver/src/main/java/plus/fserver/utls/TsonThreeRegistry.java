package plus.fserver.utls;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.*;
import plus.fserver.api.Keep;
import plus.fserver.world.generator.three.*;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.Random;
import static plus.tson.ext.TsonNumUtils.*;


@Keep
public class TsonThreeRegistry {
    public static final IBlockState JUNGLE_LOG   = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
    public static final IBlockState JUNGLE_LEAF  = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.FALSE);
    public static final IBlockState DEFAULT_LOG  = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
    public static final IBlockState DEFAULT_LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK).withProperty(BlockLeaves.CHECK_DECAY, Boolean.FALSE);
    public static final IBlockState SPRUCE_LOG   = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE);
    public static final IBlockState SPRUCE_LEAF  = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.FALSE);
    public static final IBlockState ACACIA_LOG   = Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA);
    public static final IBlockState ACACIA_LEAF  = Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.ACACIA).withProperty(BlockLeaves.CHECK_DECAY, Boolean.FALSE);
    public static final IBlockState BIRCH_LOG    = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.BIRCH);
    public static final IBlockState DARK_LOG     = Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.DARK_OAK);


    private static WorldGenAbstractTree[] buffer = new WorldGenAbstractTree[12];

    static {
        reg(1, new WorldGenTrees(false));                              //DEFAULT
        reg(2, new WorldGenBigTree(false));                            //DEFAULT BIG
        reg(3, new WorldGenTaiga1());                                        //PINE_GENERATOR
        reg(4, new WorldGenTaiga2(false));                              //SPRUCE_GENERATOR
        reg(5, new WorldGenMegaPineTree(false, false));      //MEGA_PINE_GENERATOR
        reg(6, new WorldGenMegaPineTree(false, true));       //MEGA_SPRUCE_GENERATOR
        reg(7, new WorldGenMegaJungle(false, 10,
                           20, JUNGLE_LOG, JUNGLE_LEAF));       //MEGA_JUNGLE
        reg(8, new WorldGenSwamp());                                         //SWAMP
        reg(9, new WorldGenSavannaTree(false));                        //SAVANNA
        reg(10,new WorldGenBirchTree(false, false));//BIRCH
        reg(11,new WorldGenBirchTree(false, true)); //BIG_BIRCH
    }


    public static int getOrCreateOf(TsonMap map){
        TsonObj obj = map.get("id");
        if(obj.isString()){
            return ofStrId(obj.getStr());
        }
        if(!obj.isNumber()) throw new IllegalArgumentException();
        int id = obj.getInt();
        if (getById(id) != null) return id;

        String type = map.getOrDefaultStr("type", "normal");
        switch (type){
            case "def":
            case "default":
            case "normal":
                reg(id, normalOfTson(map));break;
            case "big":
            case "big_def":
                reg(id, bigOfTson(map));break;
            case "savanna":
            case "acacia":
                reg(id, acaciaOfTson(map));break;
            case "big_jungle":
                reg(id, jungleOfTson(map));break;
            case "big5":
            case "big_jungle5":
                reg(id, jungleOfTson(map, true));break;
            case "big_pine":
                reg(id, pineOfTson(map));break;
            case "micro":
            case "shrub":
                reg(id, shrubOfTson(map));break;
        }
        return id;
    }


    private static WorldGenAbstractTree acaciaOfTson(TsonMap map) {
        return new WorldGenSavannaTree(
                notify(map), wood(map, ACACIA_LOG), leaves(map, ACACIA_LEAF),
                heightBase(map, 5), heightRandom(map, 3)
        );
    }


    private static WorldGenAbstractTree bigOfTson(TsonMap map) {
        SaplingsGenerator sapGen = saplings(map);
        float heightAtt   = calc(map, "height_attenuation", 0.618f);
        float branchSlope = calc(map, "branch_slope", 0.381f);
        float scaleW      = calc(map, "scale_width", 1.0f);
        float leafDens    = calc(map, "leaf_density", 1.0f);
        int limit         = calc(map, "height_limit", 12);
        int trunk         = calc(map,"trunk_size", 1);

        if(sapGen == null) return new WorldGenBigTree(
                notify(map), wood(map, DEFAULT_LOG), leaves(map, DEFAULT_LEAF),
                heightAtt, branchSlope, scaleW, leafDens, limit, trunk
        );
        else return new WorldGenBigTreePlus(
                notify(map), wood(map, DEFAULT_LOG), leaves(map, DEFAULT_LEAF),
                heightAtt, branchSlope, scaleW, leafDens, limit, trunk, sapGen
        );
    }


    protected static IBlockState getParam(TsonMap map, String key, IBlockState def){
        TsonObj res = map.get(key);
        return res==null?def: McTsonUtils.blockOfTson(res);
    }


    public static class Generator{
        public final int[] ids;

        public Generator(TsonList threes){
            IntArrayList list = new IntArrayList();
            for (TsonObj obj:threes){
                int weight = 1;
                int id = 0;
                if(obj.isString()) id = ofStrId(obj.getStr());
                else if(obj.isNumber()) id = obj.getInt();
                else if (obj.isMap()) {
                    id = getOrCreateOf(obj.getMap());
                    weight = calc(obj.getMap(), "weight", weight);
                }
                while (weight>0){
                    list.add(id);
                    --weight;
                }
            }
            ids = list.toIntArray();
        }
        public WorldGenAbstractTree get(Random rand){
            return buffer[ids[rand.nextInt(ids.length)]];
        }
    }


    private static WorldGenShrub shrubOfTson(TsonMap map){
        return new WorldGenShrub(
                getParam(map, "wood", DEFAULT_LOG), getParam(map, "leaves", DEFAULT_LEAF)
        );
    }


    private static boolean notify(TsonMap map){
        return map.getOrDefaultBool("notify", false);
    }


    private static IBlockState wood(TsonMap map, IBlockState def){
        return getParam(map, "wood", def);
    }


    private static IBlockState leaves(TsonMap map, IBlockState def){
        return getParam(map, "leaves", def);
    }


    private static boolean vines(TsonMap map){
        return map.getOrDefaultBool("vines", false);
    }


    private static int heightBase(TsonMap map, int def){
        return calc(map, "height_base", def);
    }


    private static int heightRandom(TsonMap map, int def){
        return calc(map, "height_random", def);
    }


    private static WorldGenTrees normalOfTson(TsonMap map){
        SaplingsGenerator sapGen = saplings(map);
        if(sapGen == null) return new WorldGenTrees(
                notify(map), heightBase(map, 4),
                wood(map, DEFAULT_LOG), leaves(map, DEFAULT_LEAF), vines(map)
        );
        return new WorldGenTreesPlus(
                notify(map), heightBase(map, 4),
                wood(map, DEFAULT_LOG), leaves(map, DEFAULT_LEAF), vines(map),
                sapGen
        );
    }


    private static WorldGenHugeTrees jungleOfTson(TsonMap map){
        return jungleOfTson(map, map.getOrDefaultBool("use5", false));
    }


    private static WorldGenHugeTrees jungleOfTson(TsonMap map, boolean use5){
        boolean notify = notify(map);
        int heightBase = heightBase(map, 10),
            heightRand = heightRandom(map, 20);

        IBlockState wood = wood(map, JUNGLE_LOG), leaves = leaves(map, JUNGLE_LEAF);

        int croneWidth = 2;
        WorldGenHugeTreesPlus.CrownCreator creator;
        TsonObj crone = map.get("crone");
        if (crone == null) {
            creator = WorldGenHugeTreesPlus.crownCreatorOfTson(null);
        } else if (crone.isMap()) {
            TsonMap croneMap = crone.getMap();
            croneWidth = croneMap.getOrDefaultInt("width", croneWidth);
            creator = WorldGenHugeTreesPlus.crownCreatorOfTson(croneMap);
        } else {
            throw new IllegalArgumentException("Param '" + crone + "' not TsonMap!");
        }

        if(use5) {
            return new WorldGenMegaJungle5(notify, heightBase, heightRand, wood, leaves, croneWidth, creator, saplings(map));
        } else {
            return new WorldGenMegaJungle4(notify, heightBase, heightRand, wood, leaves, croneWidth, creator, saplings(map));
        }
    }


    private static SaplingsGenerator saplings(TsonMap map){
        if (map.containsKey("saplings")) {
            TsonMap spgMap = map.get("saplings").getMap();
            return new SaplingsGenerator(notify(map),
                    (byte) spgMap.getOrDefaultInt("biome", 0),
                    getParam(spgMap, "block", SaplingsGenerator.PODZOL)
            );
        } else return null;
    }


    private static WorldGenHugeTreesPlus pineOfTson(TsonMap map){
        boolean notify = notify(map);
        int heightBase = heightBase(map, 13),
            heightRand = heightRandom(map, 15);
        IBlockState wood = wood(map, SPRUCE_LOG), leaves = leaves(map, SPRUCE_LEAF);

        float range = map.getOrDefaultFloat("range", 0);

        WorldGenHugeTreesPlus.CrownCreator creator;
        TsonObj crone = map.get("crone");
        if (crone == null) {
            creator = WorldGenHugeTreesPlus.crownCreatorOfTson(null);
        } else if (crone.isMap()) {
            TsonMap croneMap = crone.getMap();
            creator = WorldGenHugeTreesPlus.crownCreatorOfTson(croneMap);
        } else {
            throw new IllegalArgumentException("Param '" + crone + "' not TsonMap!");
        }
        if(range>=1){
            return new WorldGenMegaPineTreeScaled(notify, range, heightBase, heightRand, wood, leaves, creator, saplings(map));
        } else {
            return new WorldGenMegaPineTree4(notify, heightBase, heightRand, wood, leaves, creator, saplings(map));
        }
    }


    private static int ofStrId(String id){
        id = id.toLowerCase();
        switch (id){
            case "oak":
            case "def":return 1;
            case "big_oak":
            case "big_def":return 2;
            case "pine":return 3;
            case "spruce":return 4;
            case "big_pine":return 5;
            case "big_spruce":return 6;
            case "big_jungle":return 7;
            case "swamp":return 8;
            case "acacia":
            case "savanna":return 9;
            case "birch":return 10;
            case "big_birch":return 11;
        }
        throw new IllegalArgumentException("Not supported -> ["+id+"]");
    }


    private static void reg(int id, WorldGenAbstractTree tree){
        if(id==0)throw new IllegalArgumentException("Id most be >0");
        if(buffer.length<=id){
            WorldGenAbstractTree[] copy = new WorldGenAbstractTree[id+1];
            System.arraycopy(buffer, 0, copy, 0, buffer.length);
            buffer = copy;
        }
        buffer[id] = tree;
    }


    public static WorldGenAbstractTree getById(int id){
        if(id>=buffer.length)return null;
        return buffer[id];
    }


    public static int getLastId(){
        return buffer.length;
    }
}