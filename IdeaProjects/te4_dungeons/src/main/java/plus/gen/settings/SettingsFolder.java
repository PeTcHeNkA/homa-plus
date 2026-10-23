package plus.gen.settings;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import plus.tson.TsonList;
import plus.tson.TsonObj;
import java.util.ArrayList;
import java.util.Random;


public abstract class SettingsFolder {

    public abstract PlacementSettings get(Random r);


    public static SettingsFolder build(TsonObj obj){
        if(obj == null || !obj.isList())return Single.DEFAULT;
        TsonList list = obj.getList();
        if(list.isEmpty()){
            return Single.DEFAULT;
        } else {
            if(list.get(0).isList()){
                ArrayList<PlacementSettings> settings =  new ArrayList<>();
                for(TsonObj o:list){
                    TsonList llist = o.getList();
                    settings.add(parseSettings(llist));
                }
                return new Multi(settings.toArray(new PlacementSettings[0]));
            } else {
                return new Single(parseSettings(list));
            }
        }
    }


    protected static PlacementSettings parseSettings(TsonList list){
        PlacementSettings cur = Single.DEFAULT.value.copy();
        for(TsonObj o:list){
            String param = o.getStr();
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
                case "m-left":cur.setMirror(Mirror.LEFT_RIGHT);break;
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
        return cur;
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