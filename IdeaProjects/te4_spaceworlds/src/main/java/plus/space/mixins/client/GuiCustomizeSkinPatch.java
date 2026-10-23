package plus.space.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCustomizeSkin;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import plus.space.client.lscreen.LGuiMainMenu;


@Mixin(GuiCustomizeSkin.class)
public abstract class GuiCustomizeSkinPatch extends GuiScreen {
    @Shadow private String title;
    private LGuiMainMenu lGuiScreen = LGuiMainMenu.get();

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        LGuiMainMenu.open("skin_edit_bg");
        super.setWorldAndResolution(mc, width, height);
        lGuiScreen.setSize(width, height);
    }


    @Override
    public void setGuiSize(int w, int h) {
        super.setGuiSize(w, h);
        lGuiScreen.setSize(w, h);
    }


    /**
     * @author Te4hno
     * @reason player
     */
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        drawPlayer(mouseX,mouseY);
        this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 20, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }


//    @Inject(method = "actionPerformed", at=@At("HEAD"))
//    protected void actionPerformed0(GuiButton button, CallbackInfo ci) {
//        if(button.enabled && button.id >0 && button.id <=6) updateParts();
//    }


    @Override
    public void drawBackground(int tint) {
        lGuiScreen.drawImageSafe();
    }


    private EntityPlayer mcpl;
    private void drawPlayer(int mx, int my){
        if(mcpl==null){
            if(this.mc.player != null){
                mcpl = this.mc.player;
            } else {
                return;
            }
        }
        if(width>433) {
            float wc = ((this.width / 2f - 100)/2)-22;
            float wh = this.height / 6f + (24 * 4);
            drawEntityOnScree0n(  wc, wh, 50,
                     (width - (width-wc) - mx), height - (height - wh) - my - 70,
                    mcpl
            );
        }
    }


//    private void updateParts(){
//        if(this.mc.player!=null)return;
//        int i = 0;
//        for (EnumPlayerModelParts enumplayermodelparts : mc.gameSettings.getModelParts()) {
//            i |= enumplayermodelparts.getPartMask();
//        }
//        mcpl.getDataManager().set(EntityPlayerPub.getPLAYER_MODEL_FLAG(), (byte)i);
//    }


    private static void drawEntityOnScree0n(float posX, float posY, int scale,
                                           float mouseX, float mouseY,
                                            EntityLivingBase ent) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)posX, (float)posY, 50.0F);
        GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float)Math.atan(mouseY / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
        ent.renderYawOffset = (float)Math.atan(mouseX / 40.0F) * 20.0F;
        ent.rotationYaw = (float)Math.atan(mouseX / 40.0F) * 40.0F;
        ent.rotationPitch = -((float)Math.atan(mouseY / 40.0F)) * 20.0F;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        rendermanager.setRenderShadow(true);
        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}