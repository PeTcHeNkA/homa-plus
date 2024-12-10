package plus.fserver.api.economy;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;


public class EconomyEngine {
    private final String balanceId;

    public EconomyEngine(String balanceId) {
        this.balanceId = balanceId;
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