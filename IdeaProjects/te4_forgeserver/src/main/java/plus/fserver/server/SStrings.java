package plus.fserver.server;

import plus.tson.utl.Te4HashMap;


public class SStrings {
    private static final Te4HashMap<String,String> cache = new Te4HashMap<>();
    private static String re(String s){
        cache.fput(s, s);
        return s;
    }
    public static String ref(String s){
        String res = cache.get(s);
        if(res==null)cache.fput(s, s);
        return s;
    }

    public static final String Pos                 = re("Pos");
    public static final String Motion              = re("Motion");
    public static final String Rotation            = re("Rotation");
    public static final String FallDistance        = re("FallDistance");
    public static final String Fire                = re("Fire");
    public static final String Air                 = re("Air");
    public static final String OnGround            = re("OnGround");
    public static final String Dimension           = re("Dimension");
    public static final String Invulnerable        = re("Invulnerable");
    public static final String PortalCooldown      = re("PortalCooldown");
    public static final String UUID                = re("UUID");
    public static final String CustomName          = re("CustomName");
    public static final String CustomNameVisible   = re("CustomNameVisible");
    public static final String Silent              = re("Silent");
    public static final String NoGravity           = re("NoGravity");
    public static final String Glowing             = re("Glowing");
    public static final String UpdateBlocked       = re("UpdateBlocked");
    public static final String Tags                = re("Tags");
    public static final String ForgeData           = re("ForgeData");
    public static final String ForgeCaps           = re("ForgeCaps");
    public static final String Passengers          = re("Passengers");
    public static final String CanPickUpLoot       = re("CanPickUpLoot");
    public static final String PersistenceRequired = re("PersistenceRequired");
    public static final String ArmorItems          = re("ArmorItems");
    public static final String HandItems           = re("HandItems");
    public static final String ArmorDropChances    = re("ArmorDropChances");
    public static final String HandDropChances     = re("HandDropChances");
    public static final String Leashed             = re("Leashed");
    public static final String X                   = re("X");
    public static final String Y                   = re("Y");
    public static final String Z                   = re("Z");
    public static final String Leash               = re("Leash");
    public static final String LeftHanded          = re("LeftHanded");
    public static final String DeathLootTable      = re("DeathLootTable");
    public static final String DeathLootTableSeed  = re("DeathLootTableSeed");
    public static final String NoAI                = re("NoAI");
    public static final String Health              = re("Health");
    public static final String HurtTime            = re("HurtTime");
    public static final String HurtByTimestamp     = re("HurtByTimestamp");
    public static final String DeathTime           = re("DeathTime");
    public static final String AbsorptionAmount    = re("AbsorptionAmount");
    public static final String Attributes          = re("Attributes");
    public static final String ActiveEffects       = re("ActiveEffects");
    public static final String FallFlying          = re("FallFlying");
    public static final String Team                = re("Team");
}
