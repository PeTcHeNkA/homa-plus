package plus.craft;

import org.bukkit.inventory.ItemStack;
import plus.Main;

public class CraftWeapon extends CraftArtifact implements CraftThing{

    public CraftWeapon(String uid){
        super(uid);
    }
    @Override
    public ItemStack toItem(){
        return Main.generator.buildWeapon(info, stats, true);
    }
    @Override
    public ItemStack view(){
        return Main.generator.buildWeapon(info, stats, true);
    }
}