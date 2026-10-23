package plus.invlib.slot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import plus.invlib.network.PacketSyncRenderItem;
import javax.vecmath.Vector4f;
import java.util.Map;


public class BodyASlotRenderer implements LayerRenderer<EntityPlayer> {
    private static final RenderItem renderer = Minecraft.getMinecraft().getRenderItem();

    private final float scale;
    private final float r1, r2, r3, r4;
    private final int renderSlotId;


    public BodyASlotRenderer(float scale, float rotAngle, float rotX, float rotY,float rotZ, int slot){
        this.scale = scale;
        r1 = rotAngle;
        r2 = rotX;
        r3 = rotY;
        r4 = rotZ;
        renderSlotId = slot;

        Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();
        skinMap.get("default").addLayer(this);
        skinMap.get("slim").addLayer(this);
    }


    @Override
    public void doRenderLayer(EntityPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale0) {
        GlStateManager.pushMatrix();

        GlStateManager.translate(0, 0.25F, 0.15F);
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