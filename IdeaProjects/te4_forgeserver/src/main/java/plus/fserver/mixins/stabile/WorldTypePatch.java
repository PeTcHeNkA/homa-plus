package plus.fserver.mixins.stabile;

import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.FlatGeneratorInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import plus.fserver.world.generator.biome.BiomeProviderEx;
import static net.minecraft.world.WorldType.DEBUG_ALL_BLOCK_STATES;
import static net.minecraft.world.WorldType.FLAT;


@Mixin(WorldType.class)
public class WorldTypePatch {
    /**
     * @author Te4hno
     * @reason Async
     */
    @Overwrite(remap = false)
    public BiomeProvider getBiomeProvider(World world) {
        if ((Object)this == FLAT) {
            FlatGeneratorInfo inf = FlatGeneratorInfo.createFlatGeneratorFromString(world.getWorldInfo().getGeneratorOptions());
            return new BiomeProviderSingle(Biome.getBiome(inf.getBiome(), Biomes.DEFAULT));
        }
        else if ((Object)this == DEBUG_ALL_BLOCK_STATES) {
            return new BiomeProviderSingle(net.minecraft.init.Biomes.PLAINS);
        }
        else {
            return new BiomeProviderEx(world.getWorldInfo(), null);
        }
    }
}
