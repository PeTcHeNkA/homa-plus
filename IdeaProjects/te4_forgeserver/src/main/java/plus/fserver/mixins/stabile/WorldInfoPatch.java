package plus.fserver.mixins.stabile;

import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import plus.fserver.api.ElcaWorldInfo;
import plus.fserver.world.generator.ChunkGenSettingsPlus;


@Mixin(WorldInfo.class)
public abstract class WorldInfoPatch implements ElcaWorldInfo {
    @Shadow public abstract String getGeneratorOptions();
    private ChunkGenSettingsPlus settings;

    @Override
    public ChunkGenSettingsPlus getChunkGenSettings() {
        if(settings==null) settings = new ChunkGenSettingsPlus(ChunkGeneratorSettings.Factory
                .jsonToFactory(getGeneratorOptions()).build());
        return settings;
    }
}