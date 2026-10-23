package plus.mobs.utl;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.tson.TsonMap;


@SideOnly(Side.CLIENT)
public class AnimationFolder {
    public final String IDLE;   //
    public final String ATTACK; //
    public final String JUMP;   //
    public final String DEATH;  //
    public final String WALK;   //
    public final String FLY;    //
    public final String DAMAGED;//

    protected AnimationFolder(
            String idle, String attack, String jump, String death,
            String walk, String fly, String damaged) {
        IDLE = idle;
        ATTACK = attack;
        JUMP = jump;
        DEATH = death;
        WALK = walk;
        FLY = fly;
        DAMAGED = damaged;
    }

    public AnimationFolder(TsonMap map){
        IDLE = getOrDef(map, "idle", "null");
        WALK = getOrDef(map, "walk", IDLE);
        JUMP = getOrDef(map,"jump", WALK);
        FLY = getOrDef(map, "fly", WALK);
        DAMAGED = getOrDef(map, "damaged", IDLE);
        ATTACK = getOrDef(map, "attack", IDLE);
        DEATH = getOrDef(map, "death", IDLE);
    }

    private String getOrDef(TsonMap map, String key, String def){
        if(map.containsKey(key)){
            return map.getStr(key);
        } else {
            map.put(key, def);
            return def;
        }
    }
}