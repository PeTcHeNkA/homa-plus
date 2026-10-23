package plus.space.common;

import plus.space.common.utl.TsonRemapper;
import plus.tson.TsonMap;


public class SNames {
    public static final String INT_1               = "i1";
    public static final String INT_2               = "i2";
    public static final String BOOL_1              = "b1";
    public static final String BOOL_2              = "b2";

    public static final String NAME                = "nm";
    public static final String TYPE                = "tp";
    public static final String DISTANCE            = "ds";
    public static final String ORBIT               = "orb";
    public static final String POS_X               = "x";
    public static final String POS_Y               = "y";
    public static final String POS_Z               = "z";
    public static final String ID                  = "id";
    public static final String TEXTURE             = "tx";
    public static final String SIZE                = "sz";

    public static final String BIOME_ID            = "bid";
    public static final String RACE_ID             = "rs";
    public static final String DENSITY             = "dns";
    public static final String SETTINGS            = "sgs";
    public static final String PLANET              = "pnt";
    public static final String MAIN                = "man";
    public static final String BIOMES              = "bms";
    public static final String GENERATOR           = "gen";
    public static final String SYSTEMS             = "sys";
    public static final String SUN_RANGE           = "srg";

    public static final String METEOR_START_CIRCLE = "scr";
    public static final String METEOR_END_CIRCLE   = "ecr";
    public static final String METEOR_GROUP_ID     = "mtg";

    public static final String HAS_STATION         = "sti";
    public static final String HAS_PORTAL          = "prt";
    public static final String HAS_ADDITIONAL      = "ast";
    public static final String HAS_OXYGEN          = "hxy";

    public static final String ALLOW_TOGGLE_SP     = "ats";
    public static final String ENABLE_SP_DEFAULT   = "dsp";


    public static TsonMap remapRepo(TsonMap repo){
        return new TsonRemapper().addAll(new String[][]{
                {NAME, "name"},
                {TYPE, "type"},
                {DISTANCE, "distance"},
                {ORBIT, "orbit"},
                {TEXTURE, "texture"},
                {SIZE, "size"},

                {BIOME_ID, "biomeId"},
                {RACE_ID, "raceId"},
                {DENSITY, "density"},
                {SETTINGS, "settings"},
                {PLANET, "planet"},
                {MAIN, "main"},
                {BIOMES, "biomes"},
                {GENERATOR, "generator"},
                {SYSTEMS, "systems"},
                {SUN_RANGE, "sunRange"},

                {METEOR_START_CIRCLE, "meteorStartCircle"},
                {METEOR_END_CIRCLE, "meteorEndCircle"},
                {METEOR_GROUP_ID, "meteorGroupId"},
                {HAS_STATION, "hasStation"},
                {HAS_PORTAL, "hasPortal"},
                {HAS_ADDITIONAL, "hasAdditional"},
                {HAS_OXYGEN, "hasOxygen"},
                {ALLOW_TOGGLE_SP, "allowToggleSP"},
                {ENABLE_SP_DEFAULT, "enableSPDefault"},
        }).copyMapped(repo);
    }
}