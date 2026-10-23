package plus.space.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import plus.space.client.SolarMap;
import plus.space.client.StarTextureRepo;
import plus.space.client.net.SolarMapClientProvider;
import plus.space.common.GalName;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import static plus.space.client.gui.MathHelper.calc;


@SideOnly(Side.CLIENT)
public class GuiStarMap extends ETextRenderer {
    static final TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
    static final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
    static final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
    private static final float defScale = 30;
    private final SolarMap solarMap;
    private SSModel model;
    final RotManager manager = new RotManager();
    VPlanet prevRes;
    long lastClick = -1;
    private GalName initGalName; //NOT EDIT THIS PLEASE
    private GalName curName;
    SolarMap.Star currentStar;

    public GuiStarMap(SolarMap solarMap, GalName name) {
        this.initGalName = name;
        this.solarMap = solarMap;

        if(name != null) {
            GalName stName = name;
            while (stName.getParent() != null) {
                stName = stName.getParent();
            }
            solarMap.getStar(stName.getSelfName(), star -> currentStar = star);
        }

        final GalName[] cur = {initGalName};

        openModel(cur[0], new Consumer<Boolean>() {
            @Override
            public void accept(Boolean success) {
                if(!success){
                    if(cur[0] == null){
                        System.out.println("CANT LOAD BASE MAP. MAYBE ERROR?");
                        return;
                    }
                    openModel(cur[0] = cur[0].getParent(), this);
                } else {

//                    if(!model.selectByName(name, (byte) 1) && name!=null){
//                        System.out.println("PLANET ["+name+"] NOT IN CURRENT MODEL. MAYBE ERROR?");
//                    }
                }
            }
        });
    }


    private void openModel(GalName name, Consumer<Boolean> callBack){
        curName = name;
        new SSModel(GuiStarMap.this).init(name, solarMap, ssModel -> {
            if(ssModel.isValid()) {
                manager.setDefRot();
                manager.setPos(0, 0, 0);
                model = ssModel;
                if(initGalName != null){
                    model.selectMain(initGalName);
                }
            }
            if(callBack!=null){
                callBack.accept(ssModel.isValid());
            }
        });
    }


    public static void open(){
        Minecraft.getMinecraft().displayGuiScreen(
                new GuiStarMap(SolarMap.INST, SolarMapClientProvider.getCurrentNow())
        );
    }


    private int curMouseId = -1;
    private int prevMouseId;
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        curMouseId = mouseButton;
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if(mouseButton == 0 && model != null && model.isGoToHovered()){
            onClickFly(model.getSelected(false));
        }
    }


    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        curMouseId = -1;
        super.mouseReleased(mouseX, mouseY, state);
    }


    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        manager.handleScale(Mouse.getEventDWheel());
    }


//    public static double range(double f1, double f2){
//        if(f1>0){
//            if(f2>0){
//                return f1>f2 ? -(f1-f2) : f2-f1;
//            } else {
//                return f2-f1;
//            }
//        } else {
//            if(f2>0){
//                return f2-f1;
//            } else {
//                return f1<f2 ? -(f1-f2) : f2-f1;
//            }
//        }
//    }


    public void focus(VPlanet res){
        if(res==null)return;
        float scale = 1.0f;
        double[] sp = model.getSelectedPos();
        if(sp==null)return;
        manager.setScale(scale);

        double[] res0 = calc(new double[]{-sp[0], -sp[1], 0}, new double[]{0, 0, 0},
                -manager.angleY, -manager.angleX, 0);

        manager.handleMove(-res0[0]-res0[2], res0[1], 0);
        //SolarMap.Planet plt = model.getSelectedPlanet();
        if(res.obj.name.equals(curName) || (res.obj.getCountPlanets()==0))return;
        manager.lock(() -> openModel(res.obj.name, null));
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        try {
            drawScreen0(mouseX, mouseY, partialTicks);
        } catch (Exception e){
            e.printStackTrace();
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }


    //private int markRecalc = 0;
    public void drawScreen0(int mouseX, int mouseY, float partialTicks){
        if(model==null)return;
        //++markRecalc;
        super.drawScreen(mouseX, mouseY, partialTicks);
        manager.handleRot(mouseX, mouseY, curMouseId);
        float scale = manager.getScale();

        //boolean isDoubleClick = manager.isDoubleClick(curMouseId);
        double[] pos = manager.getPos();
        model.calcPositions(-pos[0], pos[1], pos[2]);

        float eMx = ((mouseX - (this.width / 2f)) / scale);
        float eMy = ((mouseY - (this.height / 2f)) / scale);

        if(curMouseId != -1 || manager.locked() || !manager.isStabilePos() /*|| markRecalc>3*/){
            //markRecalc = 0;

            if(prevMouseId == -1) {
                if (curMouseId == 0) {
                    VPlanet res = model.sellect(eMx, eMy);
                    long cur = System.currentTimeMillis();
                    if (res != null && res == prevRes && cur < lastClick + 350) {
                        focus(res);
                    }
                    lastClick = cur;
                    prevRes = res;
                } else if(curMouseId == 2){
                    manager.setScale(1f);
                    manager.setPos(0,0,0);
                    manager.setDefRot();
                    manager.lock(null);
                }
            }
        }
        prevMouseId = curMouseId;
        GlStateManager.translate(this.width/2f, this.height/2f, -600);
        if(scale != 1) GlStateManager.scale(scale,scale,scale);
        GlStateManager.pushMatrix();

        GlStateManager.translate(0, 0, 410);
        GlStateManager.pushMatrix();
        GlStateManager.rotate(180, 0, 0, 1);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
        model.translate();

        manager.rotate();
        model.draw();
        manager.unrot();
        model.drawNames();
        model.drawSellInfo(eMx, eMy);

        GlStateManager.popMatrix();
    }


    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        double speed = 3.5;
        boolean flag = false;
        if(keyCode == Keyboard.KEY_W){
            manager.handleMove(  0,       -1*speed, 0);
            flag = true;
        } else if(keyCode == Keyboard.KEY_S){
            manager.handleMove(  0,        1*speed, 0);
            flag = true;
        } else if(keyCode == Keyboard.KEY_A){
            manager.handleMove( -1*speed,  0,       0);
            flag = true;
        } else if(keyCode == Keyboard.KEY_D ){
            manager.handleMove(  1*speed,  0,       0);
            flag = true;
        } else if (keyCode == Keyboard.KEY_ESCAPE) {
            if(curName != null){
                //if(!curName.equals(initGalName)){
                openModel(curName.getParent(), null);
                return;
                //}
            }
        } else if (keyCode == Keyboard.KEY_RETURN) {
            if(model.isGoToHovered()) onClickFly(model.getSelected(false));
        }
        super.keyTyped(typedChar, keyCode);
//        if(flag){
//            double[] pos = manager.getPos();
//            model.calcPositions(-pos[0], pos[1], pos[2]);
//        }
    }


    public class RotManager {
        private boolean lastOnSp = false, prevOnSp = false;
        float angleY = 12.5f, angleX = 180 - 12.5f;
        float curAx = angleX, curAy = angleY;
        private int prevX, prevY;


        private double dx = 0, dy = 0, dz = 0;
        private double ax = 0, ay = 0, az = 0;

        private float scale = 1f, aScale = 1f;

        private boolean isStabilePos(){
            return isNear(ax, dx) && isNear(ay, dy);
        }

        public double[] getPos(){
            ax = inter(ax, dx);
            ay = inter(ay, dy);
            az = inter(az, dz);
            return new double[]{ax, ay, az};
        }
        private boolean isNear(double a, double b){
            return ((int)a*200)==((int)b*200);
        }
        public float getScale(){
            return aScale = (float) inter(aScale, scale);
        }


        public float getCuScale(){
            return aScale;
        }


        public void setScale(float scale){
            this.scale = scale;
        }


        public void handleScale(int delta){
            if (delta != 0) {
                if(delta>0){
                    if(scale<2){
                        scale+=0.07;
                    }
                } else {
                    if(scale>0.3){
                        scale-=0.07;
                    }
                }
            }
        }

        public void setPos(double x, double y, double z){
            dx=x;dy=y;dz=z;
        }

        private boolean lock = false;
        private Runnable callBack;
        public void lock(Runnable callBack){
            lock = true;
            this.callBack = callBack;
        }
        public boolean locked(){
            return lock;
        }
        public void handleMove(double x, double y, double z){
            if(lock){
                if(isStabilePos()) {
                    lock = false;
                    if(callBack != null)callBack.run();
                    callBack = null;
                } else {
                    return;
                }
            }
            dx+=x;dy+=y;dz+=z;
        }


        public void setDefRot(){
            while (curAx>360)curAx-=360;
            while (curAy>360)curAy-=360;
            while (curAx<0)curAx+=360;
            while (curAy<0)curAy+=360;
            angleY = 12.5f; angleX = 180 - 12.5f;
        }


        public void handleRot(int x, int y, int id) {
            if(id==0) {
                if ((onSp(x, y) || prevOnSp)) {
                    if (x != prevX) {
                        angleX += 0.5 * (x - prevX);
                    }
                    if (y != prevY) {
                        angleY += 0.5 * (y - prevY);
                    }
                    prevOnSp = true;
                } else {
                    prevOnSp = false;
                }
            } else if(id==1){
                double dX = prevX - x;
                double dY = prevY - y;
                double[] vec1p = model.get1pVec();

                handleMove((dX/2)*vec1p[0] + (dX/2)*vec1p[2], -(dY/2)*vec1p[1], 0);
            }
            prevX = x;
            prevY = y;
        }

        private double inter(double cur, double next){
            return (cur*0.9f)+(next*0.1f);
        }

        public void rotate(){
            curAx = (float) inter(curAx, angleX);
            curAy = (float) inter(curAy, angleY);

            GlStateManager.rotate(curAx, 0, 1, 0);
            GlStateManager.rotate(curAy, 1, 0, 0);
        }
        private void unrot(){
            GlStateManager.rotate(-curAy, 1, 0, 0);
            GlStateManager.rotate(-curAx, 0, 1, 0);
        }

        private boolean onSp(int x, int y) {
            if(1==1)return true;
            lastOnSp = isBetween(x, -6, -139) && isBetween(y, -6, -79);
            return lastOnSp;
        }
    }


    private int flyState = -1;
    public void onClickFly(VPlanet planet){
        if(flyState!=-1 || planet==null)return;
        if(model.isCanGoto()){
            GalName name = planet.obj.name;
            flyState = 1;
            solarMap.provider.sendFlyTo(name, integer -> {
                System.out.println("FLY TO ["+name+"] RESULT: "+integer);
                flyState = -1;
            });
        } else {
            openModel(planet.obj.name, null);
        }
    }


    static boolean isBetween(int val, int max, int min) {
        return val < max && val > min || val > max && val < min;
    }


    public void drawObj(double x, double y, double z, VPlanet planet){
        float scale0 = planet.scale * defScale;
        if(planet.sellected()){
            scale0 *= 1.2f;
        }
        double dz = z+planet.dz;

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();

        GlStateManager.translate(x+planet.dx,y+planet.dy,dz);
        GlStateManager.rotate(180, 0,0, 1);
        GlStateManager.scale(scale0, scale0, scale0);
        GlStateManager.enableLighting();

        int id = planet.obj.textureId;

        renderItemModel(StarTextureRepo.getById(id));

        GlStateManager.disableLighting();

        if(planet.selection != 0){
            int selection;
            switch (planet.selection){
                case 1:
                    selection = 13;break;
                case 2:
                case 3:
                default:
                    selection = 12;break;
            }
            renderItemModel(StarTextureRepo.getById(selection));
        }

        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }


    protected void drawBlur(float x, float y, float z, float scale, int id){
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        GlStateManager.translate(0, 0, z-500);
        GlStateManager.scale(scale, scale, scale);

        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);

        int size = 80;
        this.mc.getTextureManager().bindTexture(StarTextureRepo.getObjTexture(id));
        this.drawTexturedModalRect((x)/scale-(size/2),
                (y)/scale-(size/2), 0, 0, size, size, size);

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }


    public void drawTexturedModalRect(float xCoord, float yCoord, int minU, int minV, int maxU, int maxV, float scale) {
        float f = 1/scale;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(xCoord + 0.0F,        yCoord + (float)maxV, this.zLevel).tex((float)(minU) * f, (float)(minV + maxV) * f).endVertex();
        bufferbuilder.pos(xCoord + (float)maxU, yCoord + (float)maxV, this.zLevel).tex((float)(minU + maxU) * f, (float)(minV + maxV) * f).endVertex();
        bufferbuilder.pos(xCoord + (float)maxU, yCoord + 0.0F,        this.zLevel).tex((float)(minU + maxU) * f, (float)(minV) * f).endVertex();
        bufferbuilder.pos(xCoord + 0.0F,        yCoord + 0.0F,        this.zLevel).tex((float)(minU) * f, (float)(minV) * f).endVertex();
        tessellator.draw();
    }


    public static void renderItemModel(ItemStack stack) {
        renderItemModel(stack, ItemCameraTransforms.TransformType.FIXED);
    }


    public static void renderItemModel(ItemStack stack, ItemCameraTransforms.TransformType transform) {
        GlStateManager.pushMatrix();
        textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        IBakedModel bakedmodel = renderItem.getItemModelWithOverrides(stack, null, null);
        bakedmodel = ForgeHooksClient.handleCameraTransforms(bakedmodel, transform, false
        );
        renderItem.renderItem(stack, bakedmodel);

        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
        textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
    }
}