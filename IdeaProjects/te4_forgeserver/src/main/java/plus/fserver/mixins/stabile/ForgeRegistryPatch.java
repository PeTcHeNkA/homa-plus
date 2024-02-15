package plus.fserver.mixins.stabile;

import net.minecraftforge.registries.IForgeRegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraftforge.registries.ForgeRegistry;
import org.spongepowered.asm.mixin.Shadow;
import plus.fserver.api.PForgeRegistry;


@Mixin(value = ForgeRegistry.class, remap = false)
public abstract class ForgeRegistryPatch<V extends IForgeRegistryEntry<V>> implements PForgeRegistry<V> {
    @Shadow abstract int add(int id, V value);

    @Override
    public void register(int id, V obj) {
        add(id, obj);
    }
}