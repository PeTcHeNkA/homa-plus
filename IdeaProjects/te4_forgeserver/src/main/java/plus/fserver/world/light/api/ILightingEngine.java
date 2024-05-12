package plus.fserver.world.light.api;

import net.minecraft.world.*;
import net.minecraft.util.math.*;


public interface ILightingEngine {
    void scheduleLightUpdate(final EnumSkyBlock p0, final BlockPos p1);


    void processLightUpdates();


    void processLightUpdatesForType(final EnumSkyBlock p0);


    void processSync();


    void processSync(final EnumSkyBlock lightType);
}
