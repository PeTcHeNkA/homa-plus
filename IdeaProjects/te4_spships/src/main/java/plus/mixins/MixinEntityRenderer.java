package plus.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.MouseFilter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.client.camera.AMouseFilter;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @Final @Shadow @Mutable
    private MouseFilter mouseFilterXAxis;
    @Final @Shadow @Mutable
    private MouseFilter mouseFilterYAxis;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(Minecraft mcIn, IResourceManager resourceManagerIn, CallbackInfo ci){
        mouseFilterXAxis = new AMouseFilter(0.35f);
        mouseFilterYAxis = new AMouseFilter(0.35f);
    }
}