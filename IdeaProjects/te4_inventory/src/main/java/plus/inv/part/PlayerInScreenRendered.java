package plus.inv.part;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.inventory.Slot;
import plus.inv.AInventoryClientRenderer;
import plus.invlib.slot.EquipmentSlotProxy;
import plus.invlib.slot.SynchronizedRenderableSlot;
import plus.invlib.render.Updateable;
import plus.invlib.render.animate.Animation;
import plus.invlib.render.animate.PlayerPoint;

public class PlayerInScreenRendered extends AbcPart implements Updateable {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private final int defIndex = -10;
    private int lastIndex = defIndex;
    private int height;
    private int width;
    private final int zIndexEnt = 10;
    private float oldMouseX;
    private float oldMouseY;


    public PlayerInScreenRendered(AInventoryClientRenderer invrenderer) {
        super(invrenderer);
    }


    @Override
    public void update() {
        if(isEnabled()) {
            height = invrenderer.height;
            width = invrenderer.width;
            defPoint.update();
            pointS39.update();
            pointS38.update();
            pointS37.update();
            pointS36.update();
            pointSTRG.update();
            animation.update();
        }
    }


    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(enabled){
            update();
        }
    }


    public void setOldMouse(int mouseX, int mouseY){
        oldMouseX = mouseX;
        oldMouseY = mouseY;
    }


    private void updateSelectedIndex(int newIndex){
        if(newIndex != lastIndex){
            lastIndex = newIndex;
            switch (newIndex){
                default:
                    animation.setTarget(defPoint);
                    break;
                case 39:
                    animation.setTarget(pointS39);
                    break;
                case 38:
                    animation.setTarget(pointS38);
                    break;
                case 37:
                    animation.setTarget(pointS37);
                    break;
                case 36:
                    animation.setTarget(pointS36);
                    break;
                case 9:
                    animation.setTarget(pointSTRG);
            }
        }
    }


    public void drawPlayerOnScreen(){
        if(isEnabled()) {
            drawPlayerOnScreen(
                    (float) width / 2 - oldMouseX,
                    (float) (height / 2 + 75 - 50) / 2 - oldMouseY
            );
        }
    }


    private void drawPlayerOnScreen(float mouseX, float mouseY) {
        EntityPlayerSP ent = mc.player;
        float f = ent.renderYawOffset;

        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;

        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.color(1,1,1,1);
        int index = defIndex;

        Slot selectedSlot = invrenderer.hoveredSlot;

        if(selectedSlot != null){
            if(selectedSlot instanceof EquipmentSlotProxy ||
                            selectedSlot instanceof SynchronizedRenderableSlot
            ){
                if(lastIndex==defIndex){
                    float atanMouseX = (float)Math.atan(mouseX / 40.0F);
                    float atanMouseY = -(float)Math.atan(mouseY / 40.0F);

                    PlayerPoint point = defPoint.copy();
                    point.renderYawOffset = atanMouseX * 10.0F;
                    point.rotationPitch = atanMouseY * 10.0F;
                    point.rotationYawHead = atanMouseX * 40.0F;
                    point.prevRotationYawHead = atanMouseY * 20.0F;
                    animation.current = point;
                    animation.target = point;
                }
                index = selectedSlot.getSlotIndex();
            }
        }

        updateSelectedIndex(index);

        if(lastIndex==defIndex){
            PlayerPoint p = animation.get();
            p.render(ent);
            float time = animation.curTime();

            float atanMouseX = (float)Math.atan(mouseX / 40.0F);
            float atanMouseY = -(float)Math.atan(mouseY / 40.0F);
            if(time<1){
                ent.renderYawOffset = PlayerPoint.rangeInTime(
                        p.renderYawOffset, atanMouseX * 10.0F, time
                );
                ent.rotationPitch = PlayerPoint.rangeInTime(
                        p.rotationPitch, atanMouseY * 10.0F, time
                );
                ent.rotationYawHead = PlayerPoint.rangeInTime(
                        p.rotationYawHead, atanMouseX * 40.0F, time
                );
                ent.prevRotationYawHead = PlayerPoint.rangeInTime(
                        p.prevRotationYawHead, atanMouseY * 20.0F, time
                );
            } else {
                ent.renderYawOffset = atanMouseX * 10.0F;
                ent.rotationPitch = atanMouseY * 10.0F;
                ent.rotationYawHead = atanMouseX * 40.0F;
                ent.prevRotationYawHead = atanMouseY * 20.0F;
            }
        } else {
            animation.get().render(ent);
        }

        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);

        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        rendermanager.setRenderShadow(true);
        ent.renderYawOffset = f;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }


    private final PlayerPoint.Builder defPoint = new PlayerPoint.Builder() {
        @Override
        public void update() {
            int scale = height/2;
            translate((float) (width*0.5), (float) (height*1.1), zIndexEnt);
            scale((float)(-scale), (float)scale, (float)scale);
            renderYawOffset = 0.001f;
            rotationPitch = 0.001f;
            rotationYawHead = 0.001f;
            prevRotationYawHead = 0.001f;
        }
    };


    private final PlayerPoint.Builder pointS39 = new PlayerPoint.Builder(){
        @Override
        public void update() {
            int scale = height/2;
            translate((float) (width*0.5), (float) (height*1.3), zIndexEnt);
            scale*=1.2;
            scale((float)(-scale), (float)scale, (float)scale);
            renderYawOffset = 5;
            rotationPitch = 2;
            rotationYawHead = 20;
            prevRotationYawHead = 20;
        }
    };


    private final PlayerPoint.Builder pointS38 = new PlayerPoint.Builder(){
        @Override
        public void update() {
            int scale = height/2;
            translate((float) (width*0.5), (float) (height*1.25), zIndexEnt);
            scale*=1.28;
            scale((float)(-scale), (float)scale, (float)scale);
            renderYawOffset = 20;
            rotationPitch = 5;
            rotationYawHead = 5;
            prevRotationYawHead = 5;
        }
    };


    private final PlayerPoint.Builder pointS37 = new PlayerPoint.Builder(){
        @Override
        public void update() {
            int scale = height/2;
            translate((float) (width*0.5), (float) (height*0.91), zIndexEnt);
            scale*=1.2;
            scale((float)(-scale), (float)scale, (float)scale);
            renderYawOffset = 35;
            rotationPitch = 8;
            rotationYawHead = 5;
            prevRotationYawHead = -25;
        }
    };


    private final PlayerPoint.Builder pointS36 = new PlayerPoint.Builder(){
        @Override
        public void update() {
            int scale = height/2;
            translate((float) (width*0.5), (float) (height*0.75), zIndexEnt);
            scale*=1.2;
            scale((float)(-scale), (float)scale, (float)scale);

            renderYawOffset = 45;
            rotationPitch = 14;
            rotationYawHead = -10;
            prevRotationYawHead = -10;
        }
    };


    private final PlayerPoint.Builder pointSTRG = new PlayerPoint.Builder(){
        @Override
        public void update() {
            int scale = height/2;
            translate((float) (width*0.5), (float) (height*1.2), zIndexEnt);
            scale*=1.5;
            scale((float)(-scale), (float)scale, (float)scale);

            renderYawOffset = 125;
            rotationPitch = 0;
            rotationYawHead = 125;
            prevRotationYawHead = 125;
        }
    };


    private final Animation<PlayerPoint, EntityPlayerSP> animation = new Animation<PlayerPoint, EntityPlayerSP>(){
        @Override
        public void update() {
            this.current = defPoint.copy();
            this.target = defPoint.copy();
            super.update();
        }
    };
}