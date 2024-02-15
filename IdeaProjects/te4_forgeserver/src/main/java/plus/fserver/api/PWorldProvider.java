package plus.fserver.api;

import net.minecraft.world.biome.BiomeProvider;


public interface PWorldProvider {
    default void setBiomeProvider(BiomeProvider provider){throw new UnsupportedOperationException();};
}