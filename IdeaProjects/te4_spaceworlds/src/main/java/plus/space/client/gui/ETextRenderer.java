package plus.space.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import plus.space.SpaceWorldsMod;


public class ETextRenderer extends GuiScreen {
    private static final ResourceLocation icons = new ResourceLocation(SpaceWorldsMod.MOD_ID, "textures/icons.png");
    private static final float scaleTexture = 0.3f;

    public int drawEText(int ico, String text, float x, float y, int color){
        return drawEText(ico, text, x, y, color, -1);
    }

    public int drawEText(int ico, String text, float x, float y, int color, float width){
        float shift = 0;
        if(width>0){
            shift = (width - GuiStarMap.fontRenderer.getStringWidth(text))/2f;
        }
        if(ico>-1) {
            GuiStarMap.textureManager.bindTexture(icons);
            shift += drawTexture(x, y, (ico & 7)<<5, (ico>>3)<<5, scaleTexture);
        }
        return (int)(shift+1) + GuiStarMap.fontRenderer.drawString(text, x+shift, y, color, false);
    }


    private float drawTexture(float xCoord, float yCoord, float minU, float minV, float scale){
        float i32s;
        drawTexture(xCoord, yCoord, minU*scale, minV*scale, i32s = scale*32, i32s, i32s*8);
        return i32s * 1.2f;
    }


    private void drawTexture(float xCoord, float yCoord, float minU, float minV, float maxU, float maxV, float scale) {
        float f = 1/scale;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(xCoord + 0.0F,yCoord + maxV, this.zLevel).tex( minU * f,         (minV + maxV) * f).endVertex();
        bufferbuilder.pos(xCoord + maxU,yCoord + maxV, this.zLevel).tex((minU + maxU) * f, (minV + maxV) * f).endVertex();
        bufferbuilder.pos(xCoord + maxU,yCoord + 0.0F, this.zLevel).tex((minU + maxU) * f,  minV * f        ).endVertex();
        bufferbuilder.pos(xCoord + 0.0F,yCoord + 0.0F, this.zLevel).tex( minU * f,          minV * f        ).endVertex();
        tessellator.draw();
    }


    static class ICoLine{
        int ico = -1;
        float width = -1;
        String text;
        public float width(){
            if(width>-1)return width;
            if(ico>-1)return width = ((1.2f*scaleTexture*32)+1)+GuiStarMap.fontRenderer.getStringWidth(text);
            return width = GuiStarMap.fontRenderer.getStringWidth(text);
        }
    }


    static class EIcoLines{
        private int cursor = 0;
        private float width = -1;
        private ICoLine[] lines = new ICoLine[]{};

        public void set(int cursor, int ico, float width, String text){
            int oldC = this.cursor;
            this.cursor = cursor;
            add(ico, width, text);
            this.cursor = oldC;
        }

        public void add(int ico, String text){
            add(ico, -1, text);
        }

        public void add(int ico, float width, String text){
            ICoLine curLine;
            if(cursor >= lines.length){
                ICoLine[] ns = new ICoLine[cursor + 1];
                System.arraycopy(lines, 0, ns, 0, lines.length);
                lines = ns;
                curLine = lines[cursor] = new ICoLine();
            } else {
                curLine = lines[cursor];
            }
            curLine.ico = ico;
            curLine.text = text;
            curLine.width = width;
            ++cursor;
        }
        public ICoLine get(int i){
            return lines[i];
        }
        public int size(){
            return cursor;
        }
        public void clear(){
            cursor = 0;
            width = -1;
        }
        public void setWidth(float width){
            this.width = width;
        }
        public float width(){
            if(this.width>-1)return this.width;
            width = 10;
            for(int k1 = 0; k1 < size(); ++k1) {
                ICoLine line = get(k1);
                float newWidth = line.width();
                if (newWidth > width) {
                    width = newWidth;
                }
            }
            return width+1;
        }
        public float height(){
            return 8 + (size() * 10);
        }
    }


    public void drawEt(EIcoLines textLines, float x, float y, int color){
        drawEt(textLines, x, y, color, 0x70_11_11_11, 0x50_DD_40_40, 0x50_22_00_00);
    }


    public void drawEt(EIcoLines textLines, float x, float y, int color, int c1, int c2, int c3) {
        GlStateManager.pushMatrix();

        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        float width = textLines.width();

        float l1 = x + 12;
        float i2 =  y - 12;
        float k = 8 + (textLines.size() * 10);

//        if ((l1 + width)/curScale > this.width) {
//            l1 -= (28 + width)/curScale;
//        }
//
//        if ((i2 + k + 6)/curScale > this.height) {
//            i2 = (this.height - k)/curScale;
//        }

        this.drawGradientRect(l1 - 3, i2 - 4, l1 + width + 3, i2 - 3, c1, c1);
        this.drawGradientRect(l1 - 3, i2 + k + 3, l1 + width + 3, i2 + k + 4, c1, c1);
        this.drawGradientRect(l1 - 3, i2 - 3, l1 + width + 3, i2 + k + 3, c1, c1);
        this.drawGradientRect(l1 - 4, i2 - 3, l1 - 3, i2 + k + 3, c1, c1);
        this.drawGradientRect(l1 + width + 3, i2 - 3, l1 + width + 4, i2 + k + 3, c1, c1);
//        int i1 = 0x50_50_00_FF;
//        int j1 = 0x50_28_00_7F;
        this.drawGradientRect(l1 - 3, i2 - 3 + 1, l1 - 3 + 1, i2 + k + 3 - 1, c2, c3);
        this.drawGradientRect(l1 + width + 2, i2 - 3 + 1, l1 + width + 3, i2 + k + 3 - 1, c2, c3);
        this.drawGradientRect(l1 - 3, i2 - 3, l1 + width + 3, i2 - 3 + 1, c2, c2);
        this.drawGradientRect(l1 - 3, i2 + k + 2, l1 + width + 3, i2 + k + 3, c3, c3);

        i2 += 7;
        for(int k1 = 0; k1 < textLines.size(); ++k1) {
            ICoLine line = textLines.get(k1);
            drawEText(line.ico, line.text, l1, i2-1, color, line.width());
            i2 += 10;
        }

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();

        GlStateManager.scale(1.1, 1, 1.1);

        GlStateManager.popMatrix();
    }


    protected void drawGradientRect(float left, float top, float right, float bottom, int startColor, int endColor) {
        float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(right, top, this.zLevel).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, top, this.zLevel).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, bottom, this.zLevel).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(right, bottom, this.zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
}