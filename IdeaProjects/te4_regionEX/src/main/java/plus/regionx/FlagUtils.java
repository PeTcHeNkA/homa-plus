package plus.regionx;

import net.minecraft.entity.player.EntityPlayer;
import plus.regionx.data.RegionData;
import plus.regionx.data.flag.UserData;


public class FlagUtils {
    // 1 = protect blocks from users
    // 2 = protect blocks from mobs
    // 4 = allow PVP
    // 8 = allow PVE


    public static boolean canEditBlock(RegionData data, UserData userData, EntityPlayer entity){
        if(canPlayerEditBlock(data))return true;
        if(entity == null)return false;
        return userData != null && userData.isMember();
    }


    public static boolean canPVP(RegionData data, UserData attacker, UserData target){
        if(canPVP(data))return true;
        return attacker != null && attacker.isMember() && target != null && target.isMember();
    }


    public static boolean canPVE(RegionData data, UserData attacker){
        if(canPVE(data))return true;
        return attacker != null && attacker.isMember();
    }


    public static boolean canPlayerEditBlock(RegionData data){
        return data.getFlag(1);
    }


    public static boolean canMobEditBlock(RegionData data){
        return data.getFlag(2);
    }


    public static boolean canPVP(RegionData data){
        return data.getFlag(4);
    }


    public static boolean canPVE(RegionData data){
        return data.getFlag(8);
    }
}