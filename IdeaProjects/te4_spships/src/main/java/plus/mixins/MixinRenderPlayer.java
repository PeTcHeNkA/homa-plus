package plus.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.client.ISpREntity;
import plus.client.entity.ISpMemory;
import plus.client.entity.LivingBlockRenderer;
import plus.client.camera.Camera;
import plus.client.mem.Memory;
import plus.common.player.PlayerSpship;
import plus.client.entity.SpModelManager;
import plus.utls.MathUtils;

import static plus.utls.MathUtils.*;


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
        boolean isSpshipMode;

        ISpREntity spEnt = (ISpREntity) player;
        if(isSelfModel){
            isSpshipMode = ((PlayerSpship)player).isSpshipMode();
        } else {
            isSpshipMode = ((PlayerSpship)player).getSpModelId()!=-1;
        }

        if(isSpshipMode) {
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
            }
            GlStateManager.pushMatrix();
            if(!isSelfModel){
                GlStateManager.translate((float)x, (float)y, (float)z);
            }
            renderSpship(player, partialTicks, isSelfModel, spEnt);
            GlStateManager.popMatrix();
        } else {
            if(isSelfModel && Camera.isSelfSpshipActive){
                ci.cancel();
            }
            if(spEnt.isActive()) {
                spEnt.setActive(false);
                if(!isSelfModel){
                    ((PlayerSpship)player).setSpHitbox();
                }
            }
        }
    }

    float scale = 1.85f;


    private void renderSpship(AbstractClientPlayer player, float partialTicks, boolean isSelfModel, ISpREntity e){
        float rotXZ = (float) MathUtils.interpolateRotation(
                e.getPrevSpXZRot(),e.getSpXZRot(), partialTicks
        );
        float rotY = (float) MathUtils.interpolateRotation(
                e.getPrevSpYRot(), e.getSpYRot(), partialTicks
        );

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();

        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(0,0.5,0);
        GlStateManager.rotate(rotXZ, 0, 1, 0);
        GlStateManager.rotate(rotY, 1, 0, 0);

        ItemStack stack = SpModelManager.getSpModelByID(((PlayerSpship) player).getSpModelId());

        LivingBlockRenderer.renderItemModel(stack);

        GlStateManager.enableCull();
        GlStateManager.popMatrix();

//        renderItem.renderItem(stack,
//                renderItem.getItemModelWithOverrides(stack, null, null));

//        Minecraft.getMinecraft().getItemRenderer().renderItemSide(
//                player, SpModelManager.getSpModelByID(((PlayerSpship)player).getSpModelId()),
//                ItemCameraTransforms.TransformType.FIXED, false);

        //GlStateManager.enableCull();
    }
}