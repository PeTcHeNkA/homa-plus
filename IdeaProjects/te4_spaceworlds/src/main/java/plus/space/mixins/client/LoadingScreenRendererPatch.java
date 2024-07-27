package plus.space.mixins.client;

import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.McPatch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MinecraftError;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.space.client.lscreen.LGuiLoadingScreen;


@Mixin(LoadingScreenRenderer.class)
public abstract class LoadingScreenRendererPatch implements IProgressUpdate {
    @Shadow @Final private Minecraft mc;
    @Shadow private boolean loadingSuccess;
    @Shadow private long systemTime;
    @Shadow @Final private Framebuffer framebuffer;
    @Shadow private String currentlyDisplayedText;
    @Shadow private String message;
    private LGuiLoadingScreen scr;


    @Inject(method = "setLoadingProgress", at = @At("HEAD"), cancellable = true)
    public void setLoadingProgress0(int progress, CallbackInfo ci){
        if(1==1){
            ci.cancel();
        } else {
            return;
        }
        if (!McPatch.isRunning(mc)) {
            if (!this.loadingSuccess) {
                throw new MinecraftError();
            }
        }
        draw(progress);
    }


    @Override
    public void setDoneWorking() {
        scr = null;
    }


    private void draw(int progress){
        long i = Minecraft.getSystemTime();

        if (i - this.systemTime >= 100L) {
            this.systemTime = i;
            ScaledResolution scaledresolution = new ScaledResolution(this.mc);
            int j = scaledresolution.getScaleFactor();
            int k = scaledresolution.getScaledWidth();
            int l = scaledresolution.getScaledHeight();

            if (OpenGlHelper.isFramebufferEnabled()) {
                this.framebuffer.framebufferClear();
            }
            else {
                GlStateManager.clear(256);
            }

            this.framebuffer.bindFramebuffer(false);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 100.0D, 300.0D);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -200.0F);

            if (!OpenGlHelper.isFramebufferEnabled()) {
                GlStateManager.clear(16640);
            }

            try {
                if (!net.minecraftforge.fml.client.FMLClientHandler.instance().handleLoadingScreen(scaledresolution)) //FML Don't render while FML's pre-screen is rendering
                {
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferbuilder = tessellator.getBuffer();

                    if(scr==null){
                        scr = new LGuiLoadingScreen();
                        scr.setWorldAndResolution(Minecraft.getMinecraft(), 0,0);
                    }

                    scr.setGuiSize(k,l);
                    scr.drawImgFull();

                    if (progress >= 0) {
                        int i1 = 100;
                        int j1 = 2;
                        int k1 = k / 2 - 50;
                        int l1 = l / 2 + 16;
                        GlStateManager.disableTexture2D();
                        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
                        bufferbuilder.pos((double)k1, (double)l1, 0.0D).color(128, 128, 128, 255).endVertex();
                        bufferbuilder.pos((double)k1, (double)(l1 + 2), 0.0D).color(128, 128, 128, 255).endVertex();
                        bufferbuilder.pos((double)(k1 + 100), (double)(l1 + 2), 0.0D).color(128, 128, 128, 255).endVertex();
                        bufferbuilder.pos((double)(k1 + 100), (double)l1, 0.0D).color(128, 128, 128, 255).endVertex();
                        bufferbuilder.pos((double)k1, (double)l1, 0.0D).color(128, 255, 128, 255).endVertex();
                        bufferbuilder.pos((double)k1, (double)(l1 + 2), 0.0D).color(128, 255, 128, 255).endVertex();
                        bufferbuilder.pos((double)(k1 + progress), (double)(l1 + 2), 0.0D).color(128, 255, 128, 255).endVertex();
                        bufferbuilder.pos((double)(k1 + progress), (double)l1, 0.0D).color(128, 255, 128, 255).endVertex();
                        tessellator.draw();
                        GlStateManager.enableTexture2D();
                    }

                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    this.mc.fontRenderer.drawStringWithShadow(this.currentlyDisplayedText, (float)((k - this.mc.fontRenderer.getStringWidth(this.currentlyDisplayedText)) / 2), (float)(l / 2 - 4 - 16), 16777215);
                    this.mc.fontRenderer.drawStringWithShadow(this.message, (float)((k - this.mc.fontRenderer.getStringWidth(this.message)) / 2), (float)(l / 2 - 4 + 8), 16777215);
                }
            }
            catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
            this.framebuffer.unbindFramebuffer();

            if (OpenGlHelper.isFramebufferEnabled()) {
                this.framebuffer.framebufferRender(k * j, l * j);
            }

            this.mc.updateDisplay();

            try {
                Thread.yield();
            }
            catch (Exception ignored) {}
        }
    }
}