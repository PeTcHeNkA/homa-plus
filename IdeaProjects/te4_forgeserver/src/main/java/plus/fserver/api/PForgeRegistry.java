package plus.fserver.api;

import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;


public interface PForgeRegistry<V extends IForgeRegistryEntry<V>> extends IForgeRegistry<V> {
    default void register(int id, V obj){};
}