package plus.fserver.world.generator.schem;

import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import plus.fserver.world.generator.struct.IStruct;
import plus.fserver.world.generator.struct.Structure;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.utl.Tuple;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.function.Function;


public abstract class Schem implements ITSchem{
    protected static final HashMap<String, ITSchem> cache = new HashMap<>();
    public final Stolb[] stolbs;
    protected final int senX, senZ;

    Schem(Tuple<Stolb[], EntityInfo[]> tuple) {
        this.stolbs = tuple.A;
        int maxX = 0, maxZ = 0;
        for(Stolb stolb:stolbs){
            if(stolb.x>maxX)      maxX = stolb.x;
            if(stolb.z>maxZ)      maxZ = stolb.z;
        }
        this.senX = maxX>>1; this.senZ = maxZ>>1;
    }

    public static void clearCache(){
        cache.clear();
    }


    public static ITSchem getStructureOfMap(TsonMap map){
        TsonObj obj = map.get("structure");
        if(obj==null)return null;
        if(obj.isString()) return getStructureOfMap(obj.getStr(), map);
        if(obj.isNumber()){
            IStruct schem = Structure.getById(obj.getInt());
            if(schem==null)return null;
            return schem.getSchem();
        }
        throw new IllegalArgumentException();
    }



    public static ITSchem getStructureOfMap(String uri, TsonMap map) {
        //Try use cache
        if(cache.containsKey(uri)){
            return cache.get(uri);
        }

        int type;
        switch (uri.substring(0,2)){
            case "++":type=1;break;
            case "--":type=2;break;
            case "-?":type=3;break;
            case "##":type=4;break;
            case "[]":type=5;break;
            default:type=0;break;
        }

        if(type!=0){
            uri = uri.substring(2);
        }

        //Try use cache
        if(type != 4){
            ITSchem result = null;
            l1: for(String s:new String[]{"","++","--","-?","[]"}){
                ITSchem schem = cache.get(s+uri);
                if(schem != null){
                    Schem schem1 = (Schem) schem;
                    if(type == 1)      {
                        result = new SchemFast(   new Tuple<>(schem1.stolbs, null));
                        break;
                    } else if(type == 5) {
                        result = new SchemInchunk(new Tuple<>(schem1.stolbs, null));
                        break;
                    } else {
                        if (schem1 instanceof SchemNormal) {
                            Tuple<Stolb[], EntityInfo[]> tuple = new Tuple<>(schem1.stolbs, ((SchemNormal) schem1).entites);
                            switch (type) {
                                case 0:
                                    result = new SchemNormal(tuple);break l1;
                                case 2:
                                    result = new SchemSmart(tuple);break l1;
                                case 3:
                                    result = new SchemSmartUpper(tuple, map);break l1;
                            }
                        }
                    }
                }
            }
            if(result != null){
                cache.put(new String[]{"","++","--","-?","##","[]"}[type]+uri, result);
                return result;
            }
        }

        File file1 = new File(Minecraft.getMinecraft().gameDir, "templates/"+uri + ".nbt");
        if(!file1.exists()){
            file1.getParentFile().mkdirs();
            return null;
        }
        try {
            FileInputStream stream = new FileInputStream(file1);
            NBTTagCompound nbt = CompressedStreamTools.readCompressed(stream);
            if (!nbt.hasKey("DataVersion", 99)) {
                nbt.setInteger("DataVersion", 500);
            }
            ITSchem template;
            switch (type){
                default:template = SchemNormal.read(nbt);break;
                case 1:template = SchemFast.read(nbt);break;
                case 2:template = SchemSmart.read(nbt);break;
                case 3:template = SchemSmartUpper.read(nbt, map);break;
                case 4:template = SchemFrame.read(nbt);break;
                case 5:template = SchemInchunk.read(nbt);break;
            }
            cache.put(uri, template);
            return template;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public String toString() {
        for(Map.Entry<String,ITSchem> entry:cache.entrySet()){
            if(entry.getValue() == this)return "("+entry.getKey()+")";
        }
        int count = 0;
        for(Stolb s:stolbs) for(StolbInfo ignored :s.blocks) ++count;
        return "(UNNAMED "+count+" BLOCKS)";
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


    protected static class Stolb{
        public final StolbInfo[] blocks;
        public final int x;
        public final int z;
        Stolb(StolbInfo[] blocks, int x, int z) {
            this.blocks = blocks;
            this.x = x;
            this.z = z;
        }
    }
}
class StolbBuilder{
    public final int x;
    public final int z;
    private final List<Schem.StolbInfo> blocks = new ArrayList<>();

    StolbBuilder(int x, int z) {
        this.x = x;
        this.z = z;
    }
    public void add(int y, IBlockState state, NBTTagCompound tileEntityData){
//        for(Schem.StolbInfo inf:blocks){
//            if(maybeLinked(inf.state, state)){
//                blocks.add(new Schem.StolbInfo(inf.state, tileEntityData, y));
//                return;
//            }
//        }
        blocks.add(new Schem.StolbInfo(state, tileEntityData, y));
    }
    public Schem.Stolb build(){
        return new Schem.Stolb(blocks.toArray(new Schem.StolbInfo[0]), x, z);
    }
//    private static boolean maybeLinked(IBlockState state1, IBlockState state2){
//        //return false;
//        return state1.getBlock().getMetaFromState(state1) ==
//                state2.getBlock().getMetaFromState(state2);
//    }
}
class ShemBuilder{
    private final HashMap<Integer, HashMap<Integer, StolbBuilder>> data = new HashMap<>();
    private List<EntityInfo> entities = null;

    private StolbBuilder getOrCreateStolbBuilder(int x, int z){
        if(data.containsKey(x)){
            HashMap<Integer, StolbBuilder> map2 = data.get(x);
            if(map2.containsKey(z)){
                return map2.get(z);
            } else {
                StolbBuilder builder = new StolbBuilder(x, z);
                map2.put(z, builder);
                return builder;
            }
        } else {
            HashMap<Integer, StolbBuilder> map2 = new HashMap<>();
            data.put(x, map2);
            StolbBuilder builder = new StolbBuilder(x, z);
            map2.put(z, builder);
            return builder;
        }
    }


    public void add(int x, int y, int z, IBlockState state, NBTTagCompound tileEntityData){
        StolbBuilder builder = getOrCreateStolbBuilder(x, z);
        builder.add(y, state, tileEntityData);
    }

    public void addEntityes(List<EntityInfo> entities){
        this.entities = entities;
    }


    public Schem build(Function<Tuple<Schem.Stolb[], EntityInfo[]>, Schem> func){
        ArrayList<Schem.Stolb> stolbs = new ArrayList<>();
        for(HashMap<Integer, StolbBuilder> m1:data.values()){
            for(StolbBuilder builder: m1.values()){
                stolbs.add(builder.build());
            }
        }
        deDublicate(stolbs);
        return func.apply(new Tuple<>(
                stolbs.toArray(new Schem.Stolb[0]),
                entities.toArray(new EntityInfo[0])
        ));
    }


    public void deDublicate(ArrayList<Schem.Stolb> stolbs){
        for(Schem.Stolb stolb:stolbs){
            for(int cursor1=0;cursor1<stolb.blocks.length;cursor1++){
                IBlockState check = stolb.blocks[cursor1].state;
                for(Schem.Stolb stolb0:stolbs){
                    for(int cursor2=0;cursor2<stolb0.blocks.length;cursor2++){
                        Schem.StolbInfo edited = stolb0.blocks[cursor2];

                        if(maybeLinked(check, edited.state)){
                            stolb0.blocks[cursor2] = new Schem.StolbInfo(
                                    check, edited.tileEntityData, edited.y);
                        }
                    }
                }
            }
        }
    }


    private static boolean maybeLinked(IBlockState state1, IBlockState state2){
        //return false;
        return state1.getBlock() == state2.getBlock() && state1.getBlock().getMetaFromState(state1) ==
                state2.getBlock().getMetaFromState(state2);
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


    public Schem toSchem(Function<Tuple<Schem.Stolb[], EntityInfo[]>, Schem> func,
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
    final ObjectIntIdentityMap<IBlockState> ids;
    private int lastId;

    public BasicPalette() {
        this.ids = new ObjectIntIdentityMap<>(16);
    }

    public IBlockState stateFor(int id) {
        IBlockState iblockstate = this.ids.getByValue(id);
        return iblockstate == null ? BlockIDIndexes.AIR_STATE : iblockstate;
    }

    public Iterator<IBlockState> iterator() {
        return this.ids.iterator();
    }

    public void addMapping(IBlockState state, int i) {
        this.ids.put(state, i);
    }
}