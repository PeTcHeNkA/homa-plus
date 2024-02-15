//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\ExtraApps\Minecraft-Deobfuscator3000-1.2.3\1.12 stable mappings"!

//Decompiled by Procyon!

package plus.fserver.world.light.api;

import net.minecraft.world.*;
import net.minecraft.util.math.*;

public interface ILightingEngine
{
    void scheduleLightUpdate(final EnumSkyBlock p0, final BlockPos p1);
    
    void processLightUpdates();
    
    void processLightUpdatesForType(final EnumSkyBlock p0);


    void processSync();


    void processSync(final EnumSkyBlock lightType);
}
