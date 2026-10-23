package plus.fserver.utls.collections;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import java.util.HashMap;


public class PotionMap extends HashMap<Potion,PotionEffect> {
    public boolean potionsNeedUpdate;
    public PotionMap(){
        super(3);
    }
}
