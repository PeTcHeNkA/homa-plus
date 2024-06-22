package plus.fserver.api;

import net.minecraft.world.biome.BiomeProvider;


public interface ElcaWorldProvider {
    default void setBiomeProvider(BiomeProvider provider){throw new UnsupportedOperationException();};
}