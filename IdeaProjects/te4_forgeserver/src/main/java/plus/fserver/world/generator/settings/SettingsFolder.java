package plus.fserver.world.generator.settings;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import plus.fserver.utls.math.FastRandom;
import plus.tson.TsonList;
import plus.tson.TsonObj;
import java.util.Random;


public abstract class SettingsFolder {
    public final PlacementSettings get(){
        return get(FastRandom.INST);
    }

    public abstract PlacementSettings get(Random r);


    public static SettingsFolder build(TsonObj obj){
        if(obj == null)return Single.DEFAULT;
        if(obj.isList()) {
            TsonList list = obj.getList();
            if (list.isEmpty()) {
                return Single.DEFAULT;
            } else {
                PlacementSettings[] settings = new PlacementSettings[list.size()];
                for (int i=0;i<settings.length;i++) {
                    settings[i] = parseSettings(list.get(i));
                }
                if(settings.length>1) return new Multi(settings);
                else return new Single(settings[0]);
            }
        } else if(obj.isString()){
            if(obj.getStr().equalsIgnoreCase("any"))return any();
            return new Single(SettingsFolder.parseSettings(obj));
        }
        throw new IllegalArgumentException();
    }


    protected static PlacementSettings parseSettings(TsonObj obj){
        PlacementSettings cur = Single.DEFAULT.value.copy();
        if(obj.isList()) for(TsonObj o:obj.getList()) apply(cur, o.getStr());
        else apply(cur, obj.getStr());
        return cur;
    }


    private static SettingsFolder any;
    private static SettingsFolder any(){
        if(any!=null)return any;
        String[] keys1 = new String[]{"rot=90","rot=180","rot=270",""};
        String[] keys2 = new String[]{"m-left","m-front",""};
        PlacementSettings[] settings = new PlacementSettings[keys1.length* keys2.length];
        int i = 0;
        for(String key1:keys1){
            for(String key2:keys2){
                PlacementSettings cur = Single.DEFAULT.value.copy();
                apply(cur, key1);
                apply(cur, key2);
                settings[i] = cur;
                ++i;
            }
        }
        return any = new Multi(settings);
    }


    private static void apply(PlacementSettings cur, String param){
        switch (param){
            case "rot=90":
            case "rot-90":cur.setRotation(Rotation.CLOCKWISE_90);break;
            case "rot=180":
            case "rot-180":cur.setRotation(Rotation.CLOCKWISE_180);break;
            case "rot--90":
            case "rot=-90":
            case "rot=270":
            case "rot-270":cur.setRotation(Rotation.COUNTERCLOCKWISE_90);break;
            case "entities":cur.setIgnoreEntities(false);break;
            case "m-z":
            case "m-left":cur.setMirror(Mirror.LEFT_RIGHT);break;
            case "m-x":
            case "m-front":cur.setMirror(Mirror.FRONT_BACK);break;
            default:
                if(param.startsWith("ignore:")){
                    Block block = Block.getBlockFromName(param.substring(7)
                            .replace("#",":"));
                    if(block != null) {
                        cur.setReplacedBlock(block);
                    }
                }
                break;
        }
    }
}


class Single extends SettingsFolder{
    protected static final Single DEFAULT = new Single(
            (new PlacementSettings()).setChunk(null)
                    .setIgnoreEntities(true)
                    .setIgnoreStructureBlock(true)
                    .setMirror(Mirror.NONE)
                    .setRotation(Rotation.NONE)
                    .setReplacedBlock(Blocks.AIR)
    );
    protected final PlacementSettings value;

    Single(PlacementSettings value) {
        this.value = value;
    }


    @Override
    public PlacementSettings get(Random r) {
        return value;
    }
}
class Multi extends SettingsFolder{
    private final PlacementSettings[] value;

    Multi(PlacementSettings... value) {
        this.value = value;
    }


    @Override
    public PlacementSettings get(Random r) {
        return value[r.nextInt(value.length)];
    }
}