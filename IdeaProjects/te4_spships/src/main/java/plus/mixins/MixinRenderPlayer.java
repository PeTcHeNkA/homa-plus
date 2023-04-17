package plus.mixins;

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
import plus.client.entity.ISpMemory;
import plus.client.entity.LivingBlockRenderer;
import plus.client.camera.Camera;
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

        ISpMemory mem = (ISpMemory) player;

        //Memory mem = memoryCache.getOrDef(player);
        //if(isSelfModel){
            isSpshipMode = ((PlayerSpship)player).isSpshipMode();
        //} else {
        //    isSpshipMode = ((PlayerSpship)player).getSpModelId()!=-1;
        //}

        if(isSpshipMode) {
            ci.cancel();
            if(!mem.isActive()){
                mem.setActive(true);
                mem.setFreezeTimes(0);
                //mem.freezeTimes = 0;
                //mem.prevXZrot = 180-player.rotationYaw;

//                player.prevPosX = player.posX;
//                player.prevPosY = player.posY;
//                player.prevPosZ = player.posZ;

                //mem.prevYrot = (float) MathUtls.calcYrot(player);
                if(!isSelfModel){

                    ((PlayerSpship)player).setSpHitbox();
                }
            }
            GlStateManager.pushMatrix();
            if(!isSelfModel){
                GlStateManager.translate((float)x, (float)y, (float)z);
            }
            renderSpship(player, partialTicks, isSelfModel, mem);
            GlStateManager.popMatrix();
        } else {
            if(isSelfModel && Camera.isSelfSpshipActive){
                ci.cancel();
            }
            if(mem.isActive()) {
                mem.setActive(false);
                if(!isSelfModel){
                    ((PlayerSpship)player).setSpHitbox();
                }
            }
        }
    }

    //private boolean ignore = false;

    float scale = 1.85f;



//    private static double calcXZrot0(Entity entity, ISpMemory mem){
//        return Math.toDegrees(
//                Math.atan2(
//                        MathUtils.range(entity.prevPosX, mem.getPrevXZRot()),
//                        MathUtils.range(entity.prevPosZ, mem.getPrevYRot())
//                )
//        );
//    }

    //private long lastFrame = 0;

    private static double calcXZrot0(Entity player, float partialTicks, ISpMemory mem){
        double speed = calcEntitySpeed(player);

        if(speed<0.02){
            return mem.getPrevXZRot();
        }

        //long ctm = System.currentTimeMillis();

        double rot = calcXZrot(player);
        double prev = mem.getPrevXZRot();

        double res = MathUtils.interpolateRotation(prev, rot, 0.5f);

        mem.setPrevXZRot(res);

//        if(!MathUtils.isNear(rot, mem.getCurXZRot())){
//            mem.setCurXZRot(rot);
//        }

        return res;
    }


    private void renderSpship(AbstractClientPlayer player, float partialTicks, boolean isSelfModel, ISpMemory mem){
        float rotXZ;
        float rotY;

        if(false && mem.getFreezeTimes() < 20){
            rotXZ = (float) 180-player.rotationYaw;
            rotY  = 0;
            mem.setFreezeTimes(mem.getFreezeTimes()+1);
        } else {
            rotXZ = (float) calcXZrot0(player, partialTicks, mem);
            rotY  = 0;//calcYrot(player, partialTicks, mem);
        }

        if(isSelfModel){
            Camera.spshipRotY = rotY;
            Camera.spshipRotXZ = rotXZ;
        }

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