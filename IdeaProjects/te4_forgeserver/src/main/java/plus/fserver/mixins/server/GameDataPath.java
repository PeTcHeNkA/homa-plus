package plus.fserver.mixins.server;

import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraftforge.registries.GameData;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.fserver.api.ElcaBiomeRegistrator;
import java.util.function.Predicate;


@Mixin(GameData.class)
public class GameDataPath {
    @Inject(
            remap = false,
            method = "fireRegistryEvents(Ljava/util/function/Predicate;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;")
    )
    private static void register(Predicate<ResourceLocation> filter, CallbackInfo ci){
        ElcaBiomeRegistrator.registerAll();
    }
}