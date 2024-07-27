package plus.space.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiResourcePackList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import plus.space.client.lscreen.LResLocation;


@Mixin(GuiResourcePackList.class)
public abstract class GuiResourcePackListPatch extends GuiListExtended {
    public GuiResourcePackListPatch(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
    }


    @Override
    protected void drawContainerBackground(Tessellator tessellator) {
        BufferBuilder buffer = tessellator.getBuffer();
        this.mc.getTextureManager().bindTexture(LResLocation.MEH_BG);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 128.0F;
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(this.left, this.bottom, 0.0D).tex((float)this.left  / f, (float)(this.bottom + (int)this.amountScrolled) / f).color(32, 32, 32, 255).endVertex();
        buffer.pos(this.right, this.bottom, 0.0D).tex((float)this.right / f, (float)(this.bottom + (int)this.amountScrolled) / f).color(32, 32, 32, 255).endVertex();
        buffer.pos(this.right, this.top,    0.0D).tex((float)this.right / f, (float)(this.top    + (int)this.amountScrolled) / f).color(32, 32, 32, 255).endVertex();
        buffer.pos(this.left, this.top,    0.0D).tex((float)this.left  / f, (float)(this.top    + (int)this.amountScrolled) / f).color(32, 32, 32, 255).endVertex();
        tessellator.draw();
    }
}