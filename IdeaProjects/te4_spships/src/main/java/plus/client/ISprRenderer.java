package plus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.utls.MathUtils;


@SideOnly(Side.CLIENT)
public class ISprRenderer {
    static final TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
    static final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();


    public static void render(ISpREntity e, float partialTicks){
        if(!e.needRender())return;
        double rotXZ = MathUtils.interpolateRotation(
                e.getPrevSpXZRot(),e.getSpXZRot(), partialTicks
        );
        double rotY = (float) MathUtils.interpolateRotation(
                e.getPrevSpYRot(), e.getSpYRot(), partialTicks
        );

        double[] dc = e.getDCache();
        dc[1] = dc[0] = MathUtils.interpolateRotation(dc[1], dc[0],0.2f);
        dc[0] = rotXZ = MathUtils.interpolateRotation(dc[0], rotXZ, 0.2f);

        rotY = dc[2] = MathUtils.interpolateRotation(dc[2], rotY, 0.3f);

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();

        float scale = e.getScale();

        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(0,0.5,0);
        GlStateManager.rotate((float) rotXZ, 0, 1, 0);
        GlStateManager.rotate((float) rotY, 1, 0, 0);

        renderItemModel(e.getSpModel());

        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }


    static void renderItemModel(ItemStack stack) {
        textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        //this.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        renderItem.renderItem(stack,
                renderItem.getItemModelWithOverrides(stack, null, null));

        GlStateManager.cullFace(GlStateManager.CullFace.BACK);

        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
    }
}