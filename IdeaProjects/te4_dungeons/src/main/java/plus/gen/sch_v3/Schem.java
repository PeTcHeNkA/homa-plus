package plus.gen.sch_v3;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import plus.gen.schem.ITSchem;
import plus.tson.TsonMap;
import plus.tson.utl.Tuple;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.function.Function;


public abstract class Schem implements ITSchem {
    private static final HashMap<String, ITSchem> cache = new HashMap<>();
    protected final BL<BL<StolbInfo>>[] lines;


    Schem(Tuple<BL<BL<StolbInfo>>[], EntityInfo[]> tuple) {
        this.lines = tuple.A;
    }

    protected static boolean valid(BlockInfo info){
        Block b = info.blockState.getBlock();
        return b != Blocks.STRUCTURE_BLOCK && b != Blocks.BARRIER;
    }


    public static void clearCache(){
        cache.clear();
    }


    public static ITSchem getStructureOfMap(TsonMap map) {
        String uri = map.getStr("structure");

        if(cache.containsKey(uri)){
            return cache.get(uri);
        }

        int type;
        switch (uri.substring(0,2)){
            case "++":type=1;break;
            case "--":type=2;break;
            case "-?":type=3;break;
            default:type=0;break;
        }

        if(type!=0){
            uri = uri.substring(2);
        }

        File file1 = new File(Minecraft.getMinecraft().gameDir, "templates/"+uri + ".nbt");
        if(!file1.exists() || file1.isDirectory()){
            return null;
        }
        try {
            FileInputStream stream = new FileInputStream(file1);
            NBTTagCompound nbt = CompressedStreamTools.readCompressed(stream);
            if (!nbt.hasKey("DataVersion", 99)) {
                nbt.setInteger("DataVersion", 500);
            }
            ITSchem template = null;
            switch (type){
//                default:template = SchemNormal.read(nbt);break;
//                case 1:template = SchemFast.read(nbt);break;
//                case 2:template = SchemSmart.read(nbt);break;
//                case 3:template = SchemSmartUpper.read(nbt, map);break;
            }
            cache.put(uri, template);
            return template;
        } catch (Exception e){
            if(plus.MainDungeons.PROD==false) {
                e.printStackTrace();
            }
            return null;
        }
    }


    protected static class StolbInfo{
        public final IBlockState state;
        public final NBTTagCompound tileEntityData;
        public final int y;
        StolbInfo(IBlockState state, NBTTagCompound tileEntityData, int y) {
            this.state = state;
            this.tileEntityData = tileEntityData;
            this.y = y;

        }
    }


    protected static class BL<T>{
        public final T[] items;
        public final int align;

        protected BL(T[] items, int align) {
            this.items = items;
            this.align = align;

            //new AlignedObjLine<AlignedObjLine<StolbInfo>>();
        }
    }

//    protected static class Stolb{
//        public final StolbInfo[] blocks;
//        public final int x;
//        public final int z;
//        Stolb(StolbInfo[] blocks, int x, int z) {
//            this.blocks = blocks;
//            this.x = x;
//            this.z = z;
//        }
//    }
}




//class StolbBuilder{
//    public final int x;
//    public final int z;
//    private final List<Schem.StolbInfo> blocks = new ArrayList<>();
//
//    StolbBuilder(int x, int z) {
//        this.x = x;
//        this.z = z;
//    }
//    public void add(int y, IBlockState state, NBTTagCompound tileEntityData){
////        for(Schem.StolbInfo inf:blocks){
////            if(maybeLinked(inf.state, state)){
////                blocks.add(new Schem.StolbInfo(inf.state, tileEntityData, y));
////                return;
////            }
////        }
//        blocks.add(new Schem.StolbInfo(state, tileEntityData, y));
//    }
//    public Schem.Stolb build(){
//        return new Schem.Stolb(blocks.toArray(new Schem.StolbInfo[0]), x, z);
//    }
//
//    private boolean maybeLinked(IBlockState state1, IBlockState state2){
//        //return false;
//        return state1.getBlock().getMetaFromState(state1) ==
//                state2.getBlock().getMetaFromState(state2);
//    }
//}




class ShemBuilder{
    private final HashMap<Integer, HashMap<Integer, List<Schem.StolbInfo>>> data = new HashMap<>();
    private List<EntityInfo> entities = null;

    private List<Schem.StolbInfo> getOrCreateStolbInfoList(int x, int z){
        if(data.containsKey(x)){
            HashMap<Integer, List<Schem.StolbInfo>> map = data.get(x);
            if(map.containsKey(z)){
                return map.get(z);
            } else {
                List<Schem.StolbInfo> newList = new ArrayList<>();
                map.put(z, newList);
                return newList;
            }
        } else {
            HashMap<Integer, List<Schem.StolbInfo>> map = new HashMap<>();
            data.put(x, map);
            List<Schem.StolbInfo> newList = new ArrayList<>();
            map.put(z, newList);
            return newList;
        }
    }


    public void add(int x, int y, int z, IBlockState state, NBTTagCompound tileEntityData){
        getOrCreateStolbInfoList(x, z).add(new Schem.StolbInfo(state, tileEntityData, y));
    }

    public void addEntityes(List<EntityInfo> entities){
        this.entities = entities;
    }


    public Schem build(Function<Tuple<Schem.BL<Schem.BL<Schem.StolbInfo>>[], EntityInfo[]>, Schem> func){
        Schem.BL<Schem.BL<Schem.StolbInfo>>[] linesX = new Schem.BL[data.size()];
        for(int xPos:data.keySet()){
            HashMap<Integer, List<Schem.StolbInfo>> map = data.get(xPos);
            Schem.BL<Schem.StolbInfo>[] linesZ = new Schem.BL[map.size()];
            int i=0;
            for(int zPos:map.keySet()){
                List<Schem.StolbInfo> listInfo = map.get(zPos);

                Schem.BL<Schem.StolbInfo> lineZ =
                        new Schem.BL<>(listInfo.toArray(new Schem.StolbInfo[0]), zPos);
                linesZ[i] = lineZ;
                ++i;
            }
            Arrays.sort(linesZ, Comparator.comparingInt(o -> o.align));
            linesX[xPos] = new Schem.BL<>(linesZ, xPos);
        }
        Arrays.sort(linesX, Comparator.comparingInt(o -> o.align));
//        for (Schem.BL<Schem.BL<Schem.StolbInfo>> x : linesX) {
//            for (Schem.BL<Schem.StolbInfo> z:x.items){
//                z.items[0].
//            }
//        }
        return func.apply(new Tuple<>(linesX, entities.toArray(new EntityInfo[0])));
    }
}
class SchemReader{
    protected BlockInfo[] blocks;
    protected final List<EntityInfo> entities = Lists.newArrayList();
    protected BlockPos size = BlockPos.ORIGIN;

    public SchemReader read(NBTTagCompound compound) {
        this.entities.clear();
        NBTTagList nbtTagList = compound.getTagList("size", 3);
        this.size = new BlockPos(nbtTagList.getIntAt(0), nbtTagList.getIntAt(1), nbtTagList.getIntAt(2));
        BasicPalette palette = new BasicPalette();
        NBTTagList nbtTagList1 = compound.getTagList("palette", 10);

        for (int i = 0; i < nbtTagList1.tagCount(); ++i) {
            palette.addMapping(NBTUtil.readBlockState(nbtTagList1.getCompoundTagAt(i)), i);
        }

        NBTTagList nbtTagList2 = compound.getTagList("blocks", 10);

        List<BlockInfo> blocks = new ArrayList<>();
        for (int j = 0; j < nbtTagList2.tagCount(); ++j) {
            NBTTagCompound nbttagcompound = nbtTagList2.getCompoundTagAt(j);
            NBTTagList nbtTagList3 = nbttagcompound.getTagList("pos", 3);

            IBlockState iblockstate = palette.stateFor(nbttagcompound.getInteger("state"));

            NBTTagCompound nbttagcompound1;

            if (nbttagcompound.hasKey("nbt")) {
                nbttagcompound1 = nbttagcompound.getCompoundTag("nbt");
            } else {
                nbttagcompound1 = null;
            }

            blocks.add(
                    new BlockInfo(
                            nbtTagList3.getIntAt(0),
                            nbtTagList3.getIntAt(1),
                            nbtTagList3.getIntAt(2),
                            iblockstate, nbttagcompound1)
            );
        }
        this.blocks = blocks.toArray(new BlockInfo[0]);

        NBTTagList nbtTagList4 = compound.getTagList("entities", 10);

        for (int k = 0; k < nbtTagList4.tagCount(); ++k) {
            NBTTagCompound compoundTagAt = nbtTagList4.getCompoundTagAt(k);
            NBTTagList nbtTagList5 = compoundTagAt.getTagList("pos", 6);
            Vec3d vec3d = new Vec3d(nbtTagList5.getDoubleAt(0), nbtTagList5.getDoubleAt(1), nbtTagList5.getDoubleAt(2));
            NBTTagList tagListBlockPos = compoundTagAt.getTagList("blockPos", 3);
            BlockPos blockPos = new BlockPos(tagListBlockPos.getIntAt(0), tagListBlockPos.getIntAt(1), tagListBlockPos.getIntAt(2));

            if (compoundTagAt.hasKey("nbt")) {
                NBTTagCompound nbt = compoundTagAt.getCompoundTag("nbt");
                this.entities.add(new EntityInfo(vec3d, blockPos, nbt));
            }
        }
        return this;
    }


    public Schem toSchem(Function<Tuple<Schem.BL<Schem.BL<Schem.StolbInfo>>[], EntityInfo[]>, Schem> func,
                         Function<BlockInfo, Boolean> validator){
        ShemBuilder builder = new ShemBuilder();
        for(BlockInfo block:blocks){
            if(validator.apply(block)) {
                builder.add(block.x, block.y, block.z, block.blockState, block.tileentitydata);
            }
        }
        builder.addEntityes(entities);
        return builder.build(func);
    }
}
class BlockInfo {
    final IBlockState blockState;
    final NBTTagCompound tileentitydata;
    final int x;
    final int y;
    final int z;

    public BlockInfo(int x,int y, int z, IBlockState stateIn, NBTTagCompound compoundIn) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockState = stateIn;
        this.tileentitydata = compoundIn;
    }
}
class EntityInfo {
    public final Vec3d pos;
    public final BlockPos blockPos;
    public final NBTTagCompound entityData;

    public EntityInfo(Vec3d vecIn, BlockPos posIn, NBTTagCompound compoundIn) {
        this.pos = vecIn;
        this.blockPos = posIn;
        this.entityData = compoundIn;
    }
}
class BasicPalette implements Iterable<IBlockState> {
    public static final IBlockState DEFAULT_BLOCK_STATE = Blocks.AIR.getDefaultState();
    final ObjectIntIdentityMap<IBlockState> ids;
    private int lastId;

    public BasicPalette() {
        this.ids = new ObjectIntIdentityMap<>(16);
    }

    public IBlockState stateFor(int id) {
        IBlockState iblockstate = this.ids.getByValue(id);
        return iblockstate == null ? DEFAULT_BLOCK_STATE : iblockstate;
    }

    public Iterator<IBlockState> iterator() {
        return this.ids.iterator();
    }

    public void addMapping(IBlockState state, int i) {
        this.ids.put(state, i);
    }
}