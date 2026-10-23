package plus.fserver.world.generator.struct;

import net.minecraft.world.World;
import plus.fserver.world.generator.schem.ITSchem;
import plus.fserver.world.generator.settings.SettingsFolder;
import plus.fserver.world.generator.vr.MultiChunkCache;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import static plus.fserver.world.generator.schem.Schem.getStructureOfMap;


public class SimpleStruct implements IStruct{
    protected static IStruct[] buffer = new SimpleStruct[0];
    protected final SettingsFolder settings;
    protected final ITSchem template;
    public final int shiftX, shiftY, shiftZ;

    public SimpleStruct(String name){
        onInit(this, buffer.length);
        shiftX = shiftY = shiftZ = 0;
        this.template = getStructureOfMap(name, null);
        settings = SettingsFolder.build(null);
    }


    public SimpleStruct(String name, Integer shiftX, Integer shiftY, Integer shiftZ){
        onInit(this, buffer.length);
        this.shiftX = shiftX;
        this.shiftY = shiftY;
        this.shiftZ = shiftZ;
        this.template = getStructureOfMap(name, null);
        settings = SettingsFolder.build(null);
    }


    public SimpleStruct(TsonList list){
        onInit(this, list.getInt(0));
        shiftX = shiftY = shiftZ = 0;
        this.template = getStructureOfMap(list.getStr(1), null);
        settings = SettingsFolder.build(null);
    }


    public SimpleStruct(TsonMap map){
        onInit(this, map.getInt("id"));
        this.template = getStructureOfMap(map);
        settings = SettingsFolder.build(map.get("settings"));
        if(map.containsKey("pos")) {
            TsonObj pos = map.get("pos");
            if(pos.isList()) {
                TsonList list = pos.getList();
                shiftX = list.getInt(0);
                shiftY = list.getInt(1);
                shiftZ = list.getInt(2);
            } else if (pos.isNumber()) {
                shiftY = pos.getInt();;
                shiftX = shiftZ = 0;
            } else if(pos.isMap()){
                TsonMap posMap = pos.getMap();
                shiftX = posMap.getOrDefaultInt("x", 0);
                shiftY = posMap.getOrDefaultInt("y", 0);
                shiftZ = posMap.getOrDefaultInt("z", 0);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            shiftX = shiftY = shiftZ = 0;
        }
    }


    public static void onInit(IStruct struct, int ID){
        if(ID==0)throw new IllegalArgumentException("Id most be >0");
        if(buffer.length<=ID){
            IStruct[] copy = new IStruct[ID+1];
            System.arraycopy(buffer, 0, copy, 0, buffer.length);
            buffer = copy;
        }
        buffer[ID] = struct;
    }


    public void generate(World world, int x, int y, int z, StructPower power) {
        generate(world, x, y, z);
    }


    public void generate(MultiChunkCache gen, int x, int y, int z, StructPower power) {
        generate(gen, x, y, z);
    }


    @Override
    public void generate(World world, int x, int y, int z) {
        if(template==null || y>255)return;
        template.build(world,x+shiftX,y+shiftY,z+shiftZ,settings);
    }


    @Override
    public void generate(MultiChunkCache gen, int x, int y, int z) {
        if(template==null || y>255)return;
        template.placeInGen(gen,x+shiftX,y+shiftY,z+shiftZ,settings);
    }


    @Override
    public String toString() {
        if(template==null)return "EMPTY";
        if(shiftX == 0 && shiftZ == 0){
            if(shiftY == 0) return template.toString();
            return "+"+shiftY+"y:"+template;
        } else {
            return "+"+shiftX+"x+"+shiftY+"y+"+shiftZ+"z:"+ template;
        }
    }


    public static IStruct getById(int id){
        if(id>=buffer.length)return null;
        return buffer[id];
    }


    @Override
    public ITSchem getSchem() {
        return template;
    }
}