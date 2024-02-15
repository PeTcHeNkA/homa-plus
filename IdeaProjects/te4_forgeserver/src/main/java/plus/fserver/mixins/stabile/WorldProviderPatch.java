package plus.fserver.mixins.stabile;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import plus.fserver.api.PWorldProvider;


@Mixin(WorldProvider.class)
public class WorldProviderPatch implements PWorldProvider {
    @Shadow protected BiomeProvider biomeProvider;


    @Override
    public void setBiomeProvider(BiomeProvider provider){
        this.biomeProvider = provider;
    }
}