package plus.fserver.mixins.stabile;

import net.minecraft.init.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.tson.TsonFile;


@Mixin(Bootstrap.class)
public class BootstrapPatch {
    @Inject(method = "register", at = @At("TAIL"))
    private static void register(CallbackInfo ci){
        new TsonFile("preloads.tson");
    }
}