package plus.client.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import plus.client.mem.Memory;
import plus.common.spship.entity.LivingBlock;
import plus.utls.MathUtils;

import static plus.utls.MathUtils.*;


public class LivingBlockRenderer<T extends LivingBlock> extends Render<T> {
    private static final TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
    public static final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
    private static final float scale = 1f;
//    private IBakedModel model;
//    private ItemStack stack;

    public LivingBlockRenderer(RenderManager renderManager) {
        super(renderManager);
    }


    private double calcCurPos(double var, double prev, float partialTicks){
        return var+ MathUtils.range(var, prev)*partialTicks;
    }


    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks){
        if(entity.isDead)return;
        Memory mem = entity.getOrDefMemory();
        if(!mem.isActive){
            mem.isActive = true;
            mem.freezeTimes = 0;
            mem.prevXZrot = 180-entity.rotationYaw;
        }

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.translate(x,y+0.5, z);

        GlStateManager.scale(scale, scale, scale);

        GlStateManager.rotate((float) calcXZrot(entity, partialTicks, mem), 0, 1, 0);
        GlStateManager.rotate(calcYrot(entity, partialTicks, mem), 1, 0, 0);

        renderItemModel(SpModelManager.getSpModelByID(entity.getModelId()));


        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

//    public static void renderItemModel(ItemStack stack){
//        renderItemModel(stack);
//    }

    public static final ItemStack st0 = new ItemStack(Blocks.STONE);
    public static void renderItemModel(ItemStack stack) {
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


//    protected double calcXZrot(Entity player, float partialTicks, Memory mem){
//        if(isNear(player.posX, player.prevPosX) && isNear(player.posZ,player.prevPosZ)){
//            return mem.prevXZrot;
//        }
//        mem.prevXZrot = (float) MathUtls.calcXZrot(player);
//        return mem.prevXZrot;
//    }
//
//
//    protected double calcYrot(Entity player, float partialTicks, Memory mem){
//        double result = MathUtls.calcYrot(player);
//        double fresult = mem.prevYrot+MathUtls.range(mem.prevYrot,result)*partialTicks;
//        mem.prevYrot = (float) result;
//        return fresult;
//    }


    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}