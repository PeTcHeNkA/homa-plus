package plus.client.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.client.ISpREntity;
import plus.client.ISpRSelf;
import plus.client.camera.animation.ABCPoint;
import plus.client.camera.animation.Animation;
import plus.client.SpGui;
import plus.common.player.PlayerSpship;
import plus.tson.utl.Tuple;
import plus.utls.MathUtils;
import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static plus.client.SpGui.mc;


@SideOnly(Side.CLIENT)
public class Camera {
    static final GameSettings settings = Minecraft.getMinecraft().gameSettings;
    public static boolean isSelfSpshipActive = false;
    public static boolean ignoreOverridePlayerRender = false;
    public static Animation<Point3f, EntityViewRenderEvent.CameraSetup> animation =
            new Animation<>(700,new Point3f(180,0,180));

    private static boolean nextReset = false;
    public static float spshipRotXZ = 0;
    public static float spshipRotY = 0;
    private static boolean isActive;
    public static boolean stabilePos = true;
    private static int prevDir = 0;


    public static PlayerSpship getSelfSp(){
        return ((PlayerSpship)Minecraft.getMinecraft().player);
    }


    protected static void setPith(EntityViewRenderEvent.CameraSetup e, float pith){
        e.getEntity().rotationPitch = (e.getEntity().rotationPitch + pith)/2;
        e.setPitch(pith);
    }


    public static double getDeviation(ISpRSelf sp, float partialTicks){
        double deviation = sp.getSpeed() + MathUtils.range(
                sp.getSpeed(), sp.getPrevSpeed()
        )*partialTicks;

        if(deviation<0){
            deviation = -deviation;
        }
        deviation = 40 - 17*deviation;
        return deviation;
    }


    private static long lastNormTime = System.currentTimeMillis();


    public static void initEventHandlers(){
        MinecraftForge.EVENT_BUS.register(new Camera());
    }


    @SubscribeEvent
    public void interact(PlayerInteractEvent e){
        if(isSelfSpshipActive && e.isCancelable())e.setCanceled(true);
    }


    @SubscribeEvent
    public void onBlock(DrawBlockHighlightEvent e){
        if(isSelfSpshipActive)
            e.setCanceled(true);
    }


    @SubscribeEvent
    public void onRenderHand(RenderHandEvent e){
        if(isSelfSpshipActive) e.setCanceled(true);
    }


    @SubscribeEvent
    public void onCam(EntityViewRenderEvent.CameraSetup e){
        if(isSelfSpshipActive){
            Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
            if(entity==null || !(entity instanceof ISpRSelf))return;
            float partialTicks = (float) e.getRenderPartialTicks();

            ISpRSelf ispr = (ISpRSelf) entity;
//            spshipRotXZ = (float) MathUtils.interpolateRotation(
//                    ispr.getPrevSpXZRot(), ispr.getSpXZRot(), partialTicks
//            );
            spshipRotY = (float) MathUtils.interpolateRotation(
                    ispr.getPrevSpYRot(), ispr.getSpYRot(), partialTicks
            );


            if(!isActive){
                isActive = true;
                animation.stop();
                animation.current = new Point3f(e.getPitch(),e.getYaw(),3.1f);
                animation.target = animation.current;
                animation.setTarget(new Point3f(22.5f,e.getYaw(),0.0f));
            }

            Tuple<Boolean, Point3f> result = animation.getCurPoint();
            if(result.A){
                result.B.render(e);
            } else {
                float pitch = e.getPitch();
                float rY = settings.thirdPersonView==1?
                        MathUtils.range(-spshipRotY, pitch):
                        MathUtils.range(-MathUtils.calcYrot(entity), pitch);

                double deviation = getDeviation(ispr, partialTicks);
                if(deviation<0){
                    deviation = 0;
                }
                long cur = System.currentTimeMillis();
                stabilePos = false;

                //float anim = anim(cur);

                if (rY + deviation < -0.001) {
                    if(prevDir != 1){
                        lastNormTime = cur;
                    }
                    setPith(e, pitch + MathUtils.range(rY, -deviation) * anim(cur));
                    prevDir = 1;
                } else if (-rY + deviation <= -0.001) {
                    if(prevDir != -1){
                        lastNormTime = cur;
                    }
                    setPith(e, pitch + MathUtils.range(rY, deviation) * anim(cur));
                    prevDir = -1;
                } else {
                    stabilePos = true;
                    prevDir = 0;
                }
            }
        } else {
            isActive = false;
            if(nextReset){
                nextReset = false;
                if(settings.thirdPersonView==1) {
                    Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
                    if(entity==null || !(entity instanceof ISpREntity))return;
                    ISpREntity ispr = (ISpREntity) entity;
                    e.setPitch((float) ispr.getSpXZRot());
                    e.setYaw((float) ispr.getSpYRot());
                }
            }
        }
    }


    private static float anim(long cur){
        if(cur==lastNormTime)return 0f;
        long l = cur-lastNormTime;
        if(l>1000)return 0.2f;
        return l/5000f;
    }


    private static double ssinF(double max, double time){
        return sin(PI/(max*2) * time)*time;
    }

    private static int prevThirdPersonView;
    public static void onStartSelfSpship(AbstractClientPlayer player){
        //player.capabilities.allowFlying = true;
        player.capabilities.isFlying = true;


        isSelfSpshipActive = true;

        prevThirdPersonView = settings.thirdPersonView;
        settings.thirdPersonView = 1;
        SpGui.setVisible(true);

        ginGmaeSet(false);
    }


    public static void onEndSelfSpship(AbstractClientPlayer player){
        player.capabilities.isFlying = false;
        settings.thirdPersonView = prevThirdPersonView;
        nextReset = true;
        isSelfSpshipActive = false;

        ginGmaeSet(true);
    }

    private static void ginGmaeSet(boolean mode){
        GuiIngameForge.renderHotbar = mode;
        GuiIngameForge.renderHealth = mode;
        GuiIngameForge.renderExperiance = mode;
        mc.gameSettings.heldItemTooltips = mode;

        GuiIngameForge.renderArmor = mode;
    }


//    @SubscribeEvent
//    public static void onBlockBreak(BlockEvent.BreakEvent e){
//
//    }

    public static final SpOverlay overlay = new SpOverlay();


    @SubscribeEvent
    public void onRenderOverlayPart(RenderGameOverlayEvent e){
        if(isSelfSpshipActive){
            if(e.getType() == RenderGameOverlayEvent.ElementType.FOOD){
                e.setCanceled(true);
                overlay.render();
            }
//            if(e.getType() == RenderGameOverlayEvent.ElementType.){
//                e.setCanceled(true);
//            }
        }
    }


    static class Point3f extends ABCPoint<Point3f, EntityViewRenderEvent.CameraSetup> {
        public final float pith, yaw, translate;

        public Point3f(float pith, float yaw, float translate) {
            this.pith = pith;
            this.yaw = yaw;
            this.translate = translate;
        }


        @Override
        public Point3f range(Point3f p) {
            return new Point3f(
                    MathUtils.range(pith, p.pith),
                    MathUtils.range(yaw, p.yaw),
                    MathUtils.range(translate, p.translate)
            );
        }


        @Override
        public Point3f copy() {
            return new Point3f(pith, yaw, translate);
        }


        @Override
        public Point3f add(Point3f p) {
            return new Point3f(pith +p.pith, yaw +p.yaw, translate +p.translate);
        }


        @Override
        public Point3f pow(float f) {
            return new Point3f(pith *f, yaw *f, translate *f);
        }


        @Override
        public void render(EntityViewRenderEvent.CameraSetup e) {
            GlStateManager.translate(0,0,translate);
            e.getEntity().rotationPitch = pith;
            e.setPitch(pith);
        }
    }
}