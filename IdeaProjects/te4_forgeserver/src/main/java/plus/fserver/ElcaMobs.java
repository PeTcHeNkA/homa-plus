package plus.fserver;

import plus.fserver.utls.EntityLodFunc;
import plus.tson.TsonFile;
import plus.tson.TsonMap;
import static plus.tson.ext.TsonNumUtils.*;
import static plus.tson.ext.TsonAccessUtils.*;


public class ElcaMobs {
    public static final int SPIDER_RIDER_PAWN_CHANCE  ;
    public static final int ENDERMAN_CHANCE_TAKE_BLOCK;

    public static final EntityLodFunc GLOBAL   ;
    public static final EntityLodFunc WATER    ;
    public static final EntityLodFunc MONSTERS ;
    public static final EntityLodFunc ANIMALS  ;
    public static final EntityLodFunc AMBIENT  ;
    public static final EntityLodFunc CREEPER  ;
    public static final EntityLodFunc SKELETON ;
    public static final EntityLodFunc SPIDER   ;
    public static final EntityLodFunc ZOMBIE   ;
    public static final EntityLodFunc GUARDIAN ;
    public static final EntityLodFunc BLAZE    ;
    public static final EntityLodFunc ENDERMAN ;
    public static final EntityLodFunc WITCH    ;
    public static final EntityLodFunc VILLAGER ;
    public static final EntityLodFunc ITEM     ;
    public static final EntityLodFunc FALLING  ;
    public static final EntityLodFunc BAT      ;
    public static final EntityLodFunc BOAT     ;
    public static final EntityLodFunc RABBIT   ;
    public static final EntityLodFunc MINECART ;


    static {
        TsonMap config;
        try {
            config = new TsonFile("elca-mobs.tson");
            calcR(config, new TsonMap("{global=[(24), (32)]}"));
        } catch (Exception e) {
            e.printStackTrace();
            config = new TsonMap();
        }

        GLOBAL    = new EntityLodFunc(config.get("global"),   24, 44);
        WATER     = new EntityLodFunc(config.get("water"),    18, 34);
        MONSTERS  = new EntityLodFunc(config.get("monsters"), 22, 38);
        ANIMALS   = new EntityLodFunc(config.get("animals"),  18, 38);
        AMBIENT   = new EntityLodFunc(config.get("ambient"),  14, 28);
        CREEPER   = new EntityLodFunc(config.get("creeper"),  24, 34);
        SKELETON  = new EntityLodFunc(config.get("skeleton"), 24, 34);
        SPIDER    = new EntityLodFunc(config.get("spider"),   20, 30);
        ZOMBIE    = new EntityLodFunc(config.get("zombie"),   22, 34);
        GUARDIAN  = new EntityLodFunc(config.get("guardian"), 26, 36);
        BLAZE     = new EntityLodFunc(config.get("blaze"),    16, 26);
        ENDERMAN  = new EntityLodFunc(config.get("enderman"), 24, 34);
        WITCH     = new EntityLodFunc(config.get("witch"),    28, 36);
        VILLAGER  = new EntityLodFunc(config.get("villager"), 28, 36);
        ITEM      = new EntityLodFunc(config.get("item"),     10, 18);
        FALLING   = new EntityLodFunc(config.get("falling"),  16, 28);
        BAT       = new EntityLodFunc(config.get("bat"),      12, 18);
        BOAT      = new EntityLodFunc(config.get("bat"),      12, 22);
        MINECART  = new EntityLodFunc(config.get("minecart"), 24, 28);
        RABBIT    = new EntityLodFunc(config.get("rabbit"),   16, 32);


        SPIDER_RIDER_PAWN_CHANCE   = getR(config, "spider.rider_spawn",   80  );
        ENDERMAN_CHANCE_TAKE_BLOCK = getR(config, "enderman.chance_take", 2000);
    }
}