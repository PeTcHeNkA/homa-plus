package plus.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.TsonSerelizable;
import plus.utl.NumFolder;
import plus.utl.NumX1intOrX32bool;
import plus.fserver.block.BlockFx;


public class Block extends BlockFx implements TsonSerelizable {
    private final boolean isOpaqueCube;

    static final CreativeTabs TE4BLOCKS_TAB = new CreativeTabs("te4hnoblocks") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Blocks.BEDROCK);
        }
    };

    public static Material parseMaterial(String material) {
        if (material == null) {
            return Material.ROCK;
        }
        material = material.toUpperCase();
        switch (material) {
            default:         return Material.ROCK;
            case "BARRIER":  return Material.BARRIER;
            case "AIR":      return Material.AIR;
            case "GRASS":    return Material.GRASS;
            case "GROUND":   return Material.GROUND;
            case "WOOD":     return Material.WOOD;
            case "IRON":     return Material.IRON;
            case "ICE":      return Material.ICE;
            case "SAND":     return Material.SAND;
            case "CLAY":     return Material.CLAY;
            case "WEB":      return Material.WEB;
            case "LEAVES":   return Material.LEAVES;
            case "PLANTS":   return Material.PLANTS;
            case "CARPET":   return Material.CARPET;
            case "CLOTH":    return Material.CLOTH;
            case "GOURD":    return Material.GOURD;
            case "SNOW":     return Material.SNOW;
            case "LAVA":     return Material.LAVA;
            case "WATER":    return Material.WATER;
            case "CIRCUITS": return Material.CIRCUITS;
        }
    }


    public Block(String material, String id) {
        super(parseMaterial(material));
        setRegistryName(id);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);

        Block.getStateId(getDefaultState());

        isOpaqueCube = true;
        setCreativeTab(TE4BLOCKS_TAB);
    }


    public Block(String material, String id, String name) {
        this(material, id, name, true);
        setTranslationKey(name);
    }


    public Block(TsonMap map){
        super(parseMaterial(getMaterialOfMap(map)));
        isOpaqueCube = isOpaqueCube(map);
        onBlockInitFromMap(map, this);
    }


    protected static boolean isOpaqueCube(TsonMap map){
        if(map.containsKey("add")) {
            TsonList additional = map.getList("add");
            return !additional.contains("translucent");
        } else {
            return true;
        }
    }


    protected static void onBlockInitFromMap(TsonMap map, net.minecraft.block.Block block){
        block.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        String id = map.getStr("id");
        block.setRegistryName(id);

        if(map.containsKey("name")){
            block.setTranslationKey(map.getStr("name"));
        } else {
            block.setTranslationKey(id);
        }
        if(map.containsKey("tag")) {
            TsonMap tags = map.getMap("tag");
            for (String key : tags.keySet()) {
                TsonObj tag = tags.get(key);
                BlockList.app.apply(key, block, tag);
            }
        }
    }


    protected static String getMaterialOfMap(TsonMap map){
        if(map.containsKey("tag")){
            TsonMap tags = map.getMap("tag");
            TsonObj mat = tags.get("material");
            if(mat==null)return null;
            else {
                return mat.toString();
            }
        }
        return null;
    }


    public Block(String material, String id, String name, boolean isOpaqueCube) {
        super(parseMaterial(material));
        setRegistryName(id);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setTranslationKey(name);
        this.isOpaqueCube = isOpaqueCube;
    }


    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return isOpaqueCube;
    }


    protected static TsonMap genDefaultMap(net.minecraft.block.Block block){
        TsonMap map = new TsonMap();
        if(block.isOpaqueCube(null)){
            map.addList("add").add("translucent");
        }
        map.put("id", block.getRegistryName().toString());
        map.put("name", block.getTranslationKey());
        TsonMap tags = map.addMap("tags");
        tags.put("hardness",  block.getBlockHardness(null,null,null));
        tags.put("resistance", block.getExplosionResistance(null)*0.6f);
        tags.put("material", block.getMaterial(null).toString().toLowerCase());
        return map;
    }


    @Override
    public TsonMap toTson() {
        return genDefaultMap(this);
    }


    public static abstract class Parametrized extends Block {

        public Parametrized(String material, String id, String name, boolean isOpaqueCube) {
            super(material, id, name, isOpaqueCube);
        }

        abstract IProperty[] iProperties();


        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, iProperties());
        }


        @Override
        public int getMetaFromState(IBlockState state) {
            NumFolder tempFolder = new NumX1intOrX32bool();
            int i = 0;
            for(IProperty p:iProperties()){
                Comparable value = state.getValue(p);
                tempFolder.setBool(i, (boolean) value);
                i++;
            }
            return tempFolder.toInt();
        }


        @Override
        public IBlockState getStateFromMeta(int meta) {
            IBlockState state = this.getDefaultState();
            NumFolder tempFolder = new NumX1intOrX32bool(meta);
            int i = 0;
            for(IProperty p:iProperties()){
                state = state.withProperty(p, tempFolder.getBool(i));
                i++;
            }
            return state;
        }
    }
}