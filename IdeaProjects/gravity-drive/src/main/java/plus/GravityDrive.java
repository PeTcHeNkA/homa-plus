package plus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;


public class GravityDrive implements LayerRenderer<EntityPlayer> {
    private static float B1=100;
    private static float B2=100;
    private static float B3=100;
    private static float B4=100;


    @Override
    public void doRenderLayer(EntityPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        GlStateManager.pushMatrix();
        //Позиция предмета
        GlStateManager.translate(0, 0.25F, 0.15F);
        //Вращение предмета
//        GlStateManager.rotate(180F, 0, 0, 20); тут
//        System.out.printf("LS: %s LSA: %s PTA: %s AIT: %s%n", limbSwing,limbSwingAmount,partialTicks,ageInTicks);
        GlStateManager.rotate(B1*limbSwing,B2*limbSwing,B3*limbSwing,B4);
        //Размеры предмета
        GlStateManager.scale(0.85F, 0.85F, 0.85F);

        //Условие: Если игрок присел, то мы меняем положение нашего колчана.
        if (player.isSneaking()) {
            GlStateManager.rotate(-30F, 1, 0, 0);
            GlStateManager.translate(0, -0.155F, 0.04F);
        }


        //Рендер предмета(обычный из Minecraft) new ItemStack(DriveItem.INST)
        Minecraft.getMinecraft().getRenderItem().renderItem(
                player.getHeldItem(EnumHand.MAIN_HAND),
                player,
                ItemCameraTransforms.TransformType.FIXED, false);
        GlStateManager.popMatrix();

//        Minecraft.getMinecraft().getRenderManager().getSkinMap()
//                .get("slim").
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }


    public static void register(){
        GravityDrive layer = new GravityDrive();
        Minecraft.getMinecraft().getRenderManager().getSkinMap().get("default").addLayer(layer);
        Minecraft.getMinecraft().getRenderManager().getSkinMap().get("slim").addLayer(layer);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }
}
