package plus.space.common.world.pro;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;


public class PlanetWorldProvider extends WorldProvider {
    public static DimensionType PLANET;

    public static void registerIfNeed(){
        if(PLANET != null)return;
        PLANET = DimensionType.register("planet", "planet", 1002, PlanetWorldProvider.class, false);
    }


    @Override
    public DimensionType getDimensionType() {
        return PLANET;
    }
}