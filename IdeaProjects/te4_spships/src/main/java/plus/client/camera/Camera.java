package plus.client.camera;


import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.SpMain;
import plus.client.camera.animation.ABCPoint;
import plus.client.camera.animation.Animation;
import plus.client.SpGui;
import plus.client.mem.MemoryFolder;
import plus.common.player.PlayerSpship;
import plus.tson.utl.Tuple;
import plus.utls.MathUtils;

import static java.lang.Math.PI;
import static java.lang.Math.sin;


@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = SpMain.MOD_ID)
public class Camera {
    private static final GameSettings settings = Minecraft.getMinecraft().gameSettings;
    public static final MemoryFolder memoryCache = new MemoryFolder();
    public static boolean isSelfSpshipActive = false;
    public static boolean ignoreOverridePlayerRender = false;
    public static Animation<Point3f, EntityViewRenderEvent.CameraSetup> animation =
            new Animation<>(700,new Point3f(180,0,180));

    private static boolean nextReset = false;
    public static float spshipRotXZ = 0;
    public static float spshipRotY = 0;

    private static double prevDeviation = 0;
    private static boolean isActive;

    public static PlayerSpship getSelfSp(){
        return ((PlayerSpship)Minecraft.getMinecraft().player);
    }

    private static float prevPith = 0;

    private static void setPith(EntityViewRenderEvent.CameraSetup e, float pith){
        pith = (float) MathUtils.interpolateRotation(prevPith, pith, 0.5f);
        e.getEntity().rotationPitch = pith;
        e.setPitch(pith);
        prevPith = pith;
    }

    private static double prevSpeed = 0;
    private static double getDeviation(Entity entity, float partialTicks){
        double speed = MathUtils.calcEntitySpeed(entity);
        double deviation = speed;
        //prevSpeed = deviation;
        //System.out.printf("|>%s%n",deviation);
        if(deviation<0){
            deviation = -deviation;
        }
        deviation = 40 - 17*deviation;

        deviation = prevDeviation + MathUtils.range(prevDeviation, deviation) * partialTicks;

        double rs = speed-prevSpeed;
        if(rs>0.01 || rs<-0.01) {
            prevDeviation = deviation;
            prevSpeed = speed;
        }

        return deviation;
    }


    private static long lastNormTime = System.currentTimeMillis();

    //private static float prevPith = 0;
    //private static boolean stabilePos = true;

    @SubscribeEvent
    public static void onCam(EntityViewRenderEvent.CameraSetup e){
        if(isSelfSpshipActive){
            Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
            if(entity==null)return;
            float partialTicks = (float) e.getRenderPartialTicks();

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
//                if(!stabilePos){
//                    pitch = prevPith;
//                }

                float rY;
                if(settings.thirdPersonView==1) {
                    rY = MathUtils.range(-spshipRotY, pitch);
                } else {
                    rY = MathUtils.range(-MathUtils.calcYrot(entity), pitch);
                }
                double deviation = getDeviation(entity, partialTicks);

                if(deviation<0){
                    deviation = 0;
                }
                long cur = System.currentTimeMillis();

//                double rpith = MathUtils.range(prevPith, pitch);
//                if(rpith<0){
//                    rpith = -rpith;
//                }
//                boolean ignore = rpith>1 + 0.01*Minecraft.getDebugFPS();
                boolean ignore = false;
                //float anim = (float) ssinF(2,(cur-lastNormTime)/3000f);
                float anim = 1;

                if (rY + deviation < -0.001) {
                    if(ignore){
                        lastNormTime = cur;
                    } else {
                        setPith(e, pitch + MathUtils.range(rY, -deviation) * anim);
                    }
                } else if (-rY + deviation < -0.001) {
                    if(ignore){
                        lastNormTime = cur;
                    } else {
                        setPith(e, pitch + MathUtils.range(rY, deviation) * anim);
                    }
                } else if(deviation == 0){
                    if(ignore){
                        lastNormTime = cur;
                    } else {
                        setPith(e, pitch + rY * anim);
                    }
                } else {
                    if(cur-lastNormTime>500) {
                        lastNormTime = cur;
                    }
                }
                prevPith = e.getPitch();
            }
        } else {
            isActive = false;
            if(nextReset){
                nextReset = false;
                if(settings.thirdPersonView==1) {
                    e.setPitch(spshipRotXZ);
                    e.setYaw(spshipRotY);
                }
            }
        }
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
        //GuiIngameForge.renderFood = mode;
        GuiIngameForge.renderArmor = mode;
    }


//    @SubscribeEvent
//    public static void onBlockBreak(BlockEvent.BreakEvent e){
//
//    }


    @SubscribeEvent
    public static void onRenderOverlayPart(RenderGameOverlayEvent e){
        if(isSelfSpshipActive){
            if(e.getType() == RenderGameOverlayEvent.ElementType.FOOD){
                e.setCanceled(true);
            }
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