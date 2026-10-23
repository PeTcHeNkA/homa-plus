package plus.gen.struct;

import net.minecraft.world.World;
import plus.gen.schem.ITSchem;
import plus.gen.settings.SettingsFolder;
import plus.gen.utl.LazyRandom;
import plus.gen.utl.StructPower;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import java.util.Random;
import static plus.gen.schem.Schem.getStructureOfMap;


public abstract class AbcStruct implements IStruct{

    protected static IStruct[] buffer = new AbcStruct[0];
    protected static Random lazyRandom;
    protected final SettingsFolder settings;
    protected final ITSchem template;

    public final int shiftX, shiftY, shiftZ;


    public AbcStruct(TsonMap map){
        onInit(this, map.getInt("id"));
        this.template = getStructureOfMap(map);
        settings = SettingsFolder.build(map.get("settings"));
        if(map.containsKey("pos")) {
            TsonList list = map.getList("pos");
            shiftX = list.getInt(0);
            shiftY = list.getInt(1);
            shiftZ = list.getInt(2);
        } else {
            shiftX = 0;
            shiftY = 0;
            shiftZ = 0;
        }
    }


    public static void onInit(IStruct struct, int ID){
        if(buffer.length<=ID){
            IStruct[] copy = new IStruct[ID+1];
            System.arraycopy(buffer, 0, copy, 0, buffer.length);
            buffer = copy;
        }
        buffer[ID] = struct;
    }


    @Override
    public void generate(World world, Random rand, int x, int y, int z, StructPower power) {
        if(y>250 || power.use())return;
        template.build(world,x+shiftX,y+shiftY,z+shiftZ,settings,rand);
    }


    public static void init(World world){
        for(int i=0;i<buffer.length;i++) {
            IStruct istruct = buffer[i];
            if(istruct != null) {
                if (istruct instanceof AbcStruct) {
                    AbcStruct s = (AbcStruct) istruct;
                    if (s.template == null || s instanceof Structure) {
                        buffer[i] = null;
                    }
                }
            }
        }
        lazyRandom = new Random(world.getSeed());
        //lazyRandom = new LazyRandom(world.getSeed());
    }
}