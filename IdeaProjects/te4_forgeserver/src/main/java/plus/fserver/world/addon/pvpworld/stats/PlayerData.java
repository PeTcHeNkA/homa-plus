package plus.fserver.world.addon.pvpworld.stats;

import java.util.UUID;


public class PlayerData extends CombatData {
    private UUID uuid;
    private int dimToReturn;

    protected void setUUID(UUID uuid) {
        this.uuid = uuid;
    }


    public int getDimToReturn(){
        return dimToReturn;
    }


    public void setDimToReturn(int dim){
        this.dimToReturn = dim;
    }


    public UUID getUUID(){
        return uuid;
    }
}