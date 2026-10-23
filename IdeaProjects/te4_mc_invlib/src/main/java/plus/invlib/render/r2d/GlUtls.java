package plus.invlib.render.r2d;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import static org.lwjgl.opengl.GL11.*;


public class GlUtls {

    public static void noCenter(){
        glTranslatef(-1,-1,0);
    }


    public static void enableAlpha(){
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }


    public static void color(float r, float b,float g, float a){
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor4f(r,g,b,a);
    }


    public static void disableAlpha(){
        glDisable(GL_BLEND);
    }


    public static void drawSolid(
            double left, double top,
            double right, double bottom,
            float r1, float g1,float b1, float a1,
            float zIndex) {
        drawSolid(
                left, top,
                right, bottom,
                r1, g1, b1, a1,
                r1, g1, b1, a1,
                zIndex
        );
    }


    public static void drawSolid(
            double left, double top,
            double right, double bottom,
            float r1, float g1,float b1, float a1,
            float r2, float g2, float b2, float a2,
            float zIndex) {

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        //GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        GlStateManager.shadeModel(7425);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

        bufferbuilder.pos(right, top, zIndex).color(r1, g1, b1, a1).endVertex();
        bufferbuilder.pos(left, top, zIndex).color(r1, g1, b1, a1).endVertex();
        bufferbuilder.pos(left, bottom, zIndex).color(r2, g2, b2, a2).endVertex();
        bufferbuilder.pos(right, bottom, zIndex).color(r2, g2, b2, a2).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        //GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
}