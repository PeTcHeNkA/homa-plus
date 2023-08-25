package plus.client.invlib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.common.network.PacketSyncRenderItem;
import java.util.Map;


@SideOnly(Side.CLIENT)
public class BodyASlotRenderer implements LayerRenderer<EntityPlayer> {
    private static final RenderItem renderer = Minecraft.getMinecraft().getRenderItem();
    private final float scale, r1, r2, r3, r4, tX, tY, tZ;
    private final int renderSlotId;

    public BodyASlotRenderer(float scale, float rotAngle, float rotX, float rotY, float rotZ,
                             float translateX, float translateY, float translateZ,
                             int slot){
        this.scale = scale;
        r1 = rotAngle;
        r2 = rotX;
        r3 = rotY;
        r4 = rotZ;
        tX = translateX;
        tY = translateY;
        tZ = translateZ;

        renderSlotId = slot;
        Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();
        skinMap.get("default").addLayer(this);
        skinMap.get("slim").addLayer(this);
    }


    @Override
    public void doRenderLayer(EntityPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale0) {
        GlStateManager.pushMatrix();
        float ty = tY+0.25f, tz = tZ + 0.15f;

        GlStateManager.translate(tX, ty, tz);
        GlStateManager.rotate(r1, r2, r3, r4);
        GlStateManager.scale(scale, scale, scale);

        if (player.isSneaking()) {
            GlStateManager.rotate(-30F, 1, 0, 0);
            GlStateManager.translate(0, -0.155F, 0.04F);
        }
        renderer.renderItem(
                PacketSyncRenderItem.clientCache.get(player.getEntityId(), renderSlotId),
                player, ItemCameraTransforms.TransformType.FIXED, false
        );
        GlStateManager.popMatrix();
    }


    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}