package plus.invlib.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import plus.main;


public class RTexture implements Renderable{
    private static final TextureManager manager = Minecraft.getMinecraft().getTextureManager();
    private static final Tessellator tessellator = Tessellator.getInstance();
    private static final float f = 0.00390625F;

    public final ResourceLocation texture;

    public float fu1;
    public float fu2;
    public float fv1;
    public float fv2;
    public float yHeight;
    public float xWidth;

    public int xPos;
    public int yPos;
    public int zIndex;


    public RTexture(ResourceLocation texture) {
        this.texture = texture;
    }


    public RTexture(String resPath) {
        this.texture = new ResourceLocation(main.MOD_ID+":"+resPath);
    }


    public void draw(){
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        manager.bindTexture(texture);

        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

        bufferbuilder.pos(xPos,      yHeight, zIndex).tex(fu1, fv1).endVertex();
        bufferbuilder.pos(xWidth, yHeight, zIndex).tex(fu2, fv1).endVertex();
        bufferbuilder.pos(xWidth, yPos, zIndex).tex(fu2, fv2).endVertex();
        bufferbuilder.pos(xPos, yPos, zIndex).tex(fu1, fv2).endVertex();

        tessellator.draw();
    }


    public RTexture update(int xPos, int yPos, int zIndex, int shiftTextureX, int shiftTextureY, int width, int height){
        this.xPos = xPos;
        this.yPos = yPos;
        this.zIndex = zIndex;
        fu1 = (float)(shiftTextureX) * f;
        fu2 = (float)(shiftTextureX + width) * f;

        fv1 = (float)(shiftTextureY + height) * f;
        fv2 = (float)(shiftTextureY) * f;

        yHeight = this.yPos + height;
        xWidth = this.xPos + width;
        return this;
    }
}