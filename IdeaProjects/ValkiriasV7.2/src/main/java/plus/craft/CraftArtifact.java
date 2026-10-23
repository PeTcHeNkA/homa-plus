package plus.craft;

import org.bukkit.inventory.ItemStack;
import plus.item.BaseInfo;
import plus.item.BoostStats;
import plus.Main;

public class CraftArtifact implements CraftThing{

    protected final BaseInfo info;
    protected final BoostStats stats;

    public CraftArtifact(String uid){
        if(uid.contains("=")){
            String[] args = uid.split("=");
            info = new BaseInfo(args[0]);

            if(args.length>1) {
                stats = new BoostStats(args[1]);
            } else stats = null;
        } else {
            info = new BaseInfo(uid);
            stats = null;
        }
    }
    @Override
    public ItemStack toItem(){
        return Main.generator.buildArtifact(info, stats, true);
    }
    @Override
    public ItemStack view(){
        return Main.generator.buildArtifact(info, stats, true);
    }

    public boolean isSuccessful(ItemStack item) {
        return info.equals(Main.generator.getBaseInfo(item));
    }
}