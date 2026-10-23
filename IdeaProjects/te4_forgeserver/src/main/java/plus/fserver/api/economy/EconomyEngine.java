package plus.fserver.api.economy;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import java.util.HashMap;


public class EconomyEngine {
    private static ImmutableMap<String, EconomyEngine> engines = ImmutableMap.of();
    private final String balanceId;

    public EconomyEngine(String balanceId) {
        this.balanceId = balanceId;
    }


    public static EconomyEngine byId(String id){
        EconomyEngine eng = engines.get(id);
        if(eng == null){
            synchronized (engines){
                eng = new EconomyEngine(id);
                HashMap<String, EconomyEngine> copy = new HashMap<>(engines);
                copy.put(id, eng);
                engines = ImmutableMap.copyOf(copy);
            }
        }
        return eng;
    }


    public long getPlayerBalance(EntityPlayerMP player){
        NBTTagCompound raw = player.getPersistedRaw();
        if(raw == null)return 0;
        return raw.getLong(balanceId);
    }


    public void setPlayerBalance(EntityPlayerMP player, long balance){
        NBTTagCompound raw = player.getPersistedRaw();
        if(raw == null){
            if(balance == 0)return;
            raw = player.getPersisted();
        }
        raw.setLong(balanceId, balance);
    }


    public long addPlayerBalance(EntityPlayerMP player, long balance){
        return player.getPersisted().editLongMut(balanceId, balance);
    }
}