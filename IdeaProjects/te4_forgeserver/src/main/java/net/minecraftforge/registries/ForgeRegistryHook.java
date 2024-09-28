package net.minecraftforge.registries;


public class ForgeRegistryHook {
    public static <V extends IForgeRegistryEntry<V>> void register(ForgeRegistry<V> registry, int id, V obj) {
        registry.add(id, obj);
    }
}