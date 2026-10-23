package plus.space.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiSnooper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.space.client.lscreen.LResLocation;
import java.util.List;


@SideOnly(Side.CLIENT)
public class GuiSnooperList extends GuiSlot {
    private final List<String> keys, values;
    private final FontRenderer renderer;

    public GuiSnooperList(GuiSnooper gui, FontRenderer renderer, List<String> keys, List<String> values) {
        super(gui.mc, gui.width, gui.height, 80, gui.height - 40, renderer.FONT_HEIGHT + 1);
        this.keys = keys;
        this.values = values;
        this.renderer = renderer;
    }


    protected int getSize() {
        return keys.size();
    }


    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {}


    protected boolean isSelected(int slotIndex)
    {
        return false;
    }


    protected void drawBackground() {}


    protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {
        renderer.drawString(keys.get(slotIndex), 10, yPos, 16777215);
        renderer.drawString(values.get(slotIndex), 230, yPos, 16777215);
    }


    protected int getScrollBarX() {
        return this.width - 10;
    }


    @Override
    protected void drawContainerBackground(Tessellator tessellator) {}


    @Override
    protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        this.mc.getTextureManager().bindTexture(LResLocation.MEH_BG);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 128.0F;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(this.left, endY, 0.0D).tex(0.0D, (float)endY / f).color(64, 64, 64, endAlpha).endVertex();
        bufferbuilder.pos(this.left + this.width, endY, 0.0D).tex((float)this.width / f, (float)endY / f).color(64, 64, 64, endAlpha).endVertex();
        bufferbuilder.pos(this.left + this.width, startY, 0.0D).tex((float)this.width / f, (float)startY / f).color(64, 64, 64, startAlpha).endVertex();
        bufferbuilder.pos(this.left, startY, 0.0D).tex(0.0D, (float)startY / f).color(64, 64, 64, startAlpha).endVertex();
        tessellator.draw();
    }
}