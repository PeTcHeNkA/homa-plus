package plus.mixins;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.client.ISpREntity;
import plus.client.ISprRenderer;
import plus.client.camera.Camera;
import plus.common.player.PlayerSpship;
import plus.utls.MathUtils;


@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer extends RenderLivingBase<AbstractClientPlayer>{
    @Shadow protected abstract void renderLivingAt(AbstractClientPlayer entityLivingBaseIn, double x, double y, double z);

    public MixinRenderPlayer(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }


    @Inject(method = "doRender*", at = @At("HEAD"), cancellable = true)
    public void doRender(AbstractClientPlayer player, double x, double y,
                         double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if(Camera.ignoreOverridePlayerRender)return;
        boolean isSelfModel = player.isUser();

        ISpREntity spEnt = (ISpREntity) player;

        if(((PlayerSpship)player).isSpshipMode()) {
            ci.cancel();
            if(!spEnt.isActive()){
                spEnt.setActive(true);
                spEnt.clearSpRot();
                player.prevPosX = player.posX;
                player.prevPosY = player.posY;
                player.prevPosZ = player.posZ;
                if(!isSelfModel){
                    ((PlayerSpship)player).setSpHitbox();
                }
                double[] dc = new double[3];
                spEnt.setDCache(dc);
                dc[0] = dc[1] = MathUtils.interpolateRotation(
                        spEnt.getPrevSpXZRot(),spEnt.getSpXZRot(), partialTicks
                );
            }
            GlStateManager.pushMatrix();
            if(!isSelfModel){
                GlStateManager.translate((float)x, (float)y, (float)z);
            }
            //TODO во время посадки в корабль идет поворот к старому углу
            ISprRenderer.render(spEnt, partialTicks);

            GlStateManager.popMatrix();
        } else {
            if(isSelfModel && Camera.isSelfSpshipActive){
                ci.cancel();
            }
            if(spEnt.isActive()) {
                spEnt.setActive(false);
                spEnt.setDCache(null);
                if(!isSelfModel){
                    ((PlayerSpship)player).setSpHitbox();
                }
            }
        }
    }
}