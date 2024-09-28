package plus.fserver.world.light;

import net.minecraft.world.*;
import net.minecraft.util.math.*;


public interface ILightingEngine {
    void scheduleLightUpdate(final EnumSkyBlock p0, final BlockPos p1);


    void scheduleLightUpdate(final EnumSkyBlock p0, final int x, final int y, final int z);


    void processLightUpdates();


    default void awaitUnlock(){};


    void processLightUpdatesForType(final EnumSkyBlock p0);


    void processSync();


    void processSync(final EnumSkyBlock lightType);
}
