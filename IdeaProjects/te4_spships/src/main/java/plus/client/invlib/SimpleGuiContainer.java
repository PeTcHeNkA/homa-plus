package plus.client.invlib;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public abstract class SimpleGuiContainer extends GuiContainer {
    public static int COLOR_WHITE = 16777215;
    public static int COLOR_GRAY = 4210752;

    private final ResourceLocation texture;
    public SimpleGuiContainer(ResourceLocation texture, Container inventorySlotsIn) {
        super(inventorySlotsIn);
        this.texture = texture;
    }


    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.mc.getTextureManager().bindTexture(texture);
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        onDraw(i, j);
    }


    public static void drawTranslatedString(String key, int x, int y, int color, Object... args){
        drawString(I18n.translateToLocalFormatted("gui."+key, args), x, y, color);
    }


    public static void drawString(String text, int x, int y, int color){
        RString.fontRenderer.drawString(text, x, y, color);
    }


    protected void onDraw(int widthStart, int heightStart){}
}
