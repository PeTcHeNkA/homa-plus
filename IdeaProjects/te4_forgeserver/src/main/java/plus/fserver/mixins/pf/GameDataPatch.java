package plus.fserver.mixins.pf;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import java.util.Locale;


@Mixin(value = GameData.class, remap = false)
public class GameDataPatch {
    /**
     * @author Te4hno
     * @reason boost
     */
    @Overwrite
    public static ResourceLocation checkPrefix(String name, boolean warnOverrides) {
        final int separator = name.lastIndexOf(':');
        String namespace;
        final String path = separator == -1 ? name : name.substring(separator + 1);
        if (separator == -1) {
            final ModContainer activeMod = Loader.instance().activeModContainer();
            if (activeMod != null) {
                namespace = activeMod instanceof InjectedModContainer &&
                        ((InjectedModContainer) activeMod).wrappedContainer
                                instanceof FMLContainer ? "minecraft" : activeMod.getModId().toLowerCase(Locale.ROOT);
            } else {
                namespace = "";
            }
        } else {
            namespace = name.substring(0, separator).toLowerCase(Locale.ROOT);
        }
        return new ResourceLocation(namespace, path);
    }
}