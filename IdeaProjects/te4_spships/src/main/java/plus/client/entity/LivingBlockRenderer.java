package plus.client.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import plus.client.ISprRenderer;
import plus.common.spship.entity.LivingBlock;
import plus.utls.MathUtils;


public class LivingBlockRenderer<T extends LivingBlock> extends Render<T> {
    public LivingBlockRenderer(RenderManager renderManager) {
        super(renderManager);
    }


    private double calcCurPos(double var, double prev, float partialTicks){
        return var+ MathUtils.range(var, prev)*partialTicks;
    }


    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks){
        if(entity.isDead)return;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);

        ISprRenderer.render(entity, partialTicks);
        GlStateManager.popMatrix();
    }


    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}