package plus.space.client.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.space.client.SolarMap;
import plus.space.client.StarTextureRepo;
import plus.space.common.GalName;
import plus.space.common.MBridge;
import plus.space.common.CommonCalculator;

import java.util.function.Consumer;
import static plus.space.client.gui.MathHelper.calc;


@SideOnly(Side.CLIENT)
public class SSModel {
    private final GuiStarMap guiStarMap;
    //private SolarMap.Planet[] planets0;
//    private SolarMap.MapObj central;
    private VPlanet[] planets;
    private double[][] dumpPos;
    private double x = 0, y = 0, z = 0;
    private double dx, dy, dz;
    private int selection = -1;
    private int msel = -1;
    private boolean needUpdate = false;

    public SSModel(GuiStarMap guiStarMap){
        this.guiStarMap = guiStarMap;
        planets = new VPlanet[0];
        dumpPos = new double[][]{};
    }


    public SSModel init(GalName name, SolarMap map, Consumer<SSModel> onInit){
        map.getSystems(stars -> {
            if(name==null){
                needUpdate = false;
                planets = new VPlanet[stars.length];
                dumpPos = new double[stars.length][];
                for (int i=0;i< planets.length;i++){
                    SolarMap.Star star = stars[i];
                    VPlanet pl = planets[i] = new VPlanet(star);
                    pl.setPos(star.xPos, star.yPos, star.zPos);
                }
                updateSizes();
                msel = selection = -1;
                onInit.accept(this);
            } else {
                map.getPlanets(name, result -> {
                    SolarMap.Planet[] planets0 = result.B;
                    planets = new VPlanet[planets0.length+1];
                    needUpdate = true;
                    if(planets0.length!=0) {
                        SolarMap.MapObj central = result.A;
                        dumpPos = new double[planets.length][];
                        planets[0] = new VPlanet(central, central.scale * 2.5f);
                        for (int i = 1; i < planets.length; i++) {
                            SolarMap.Planet planet = planets0[i - 1];
                            planets[i] = new VPlanet(planet);
                        }
                    }
                    msel = selection = -1;
                    onInit.accept(this);
                });
            }
        });
        return this;
    }


    public boolean isValid(){
        return !needUpdate || planets.length>1;
    }


    private void updatePlanetsIfNeed(){
        if(!needUpdate)return;
        long cur = System.currentTimeMillis();
        for (int i=1;i< planets.length;i++){
            double[] pos = getPosOf(cur, ((SolarMap.Planet)planets[i].obj).orbit);
            planets[i].setPos(pos[0], pos[1], pos[2]);
        }
        updateSizes();
    }


    private void updateSizes(){
        double maxDx = 0, minDx = 0, maxDy = 0, minDy = 0, maxDz = 0, minDz = 0;
        for (VPlanet planet : planets) {
            if (planet.dx > maxDx) maxDx = planet.dx;
            if (planet.dx < minDx) minDx = planet.dx;

            if (planet.dy > maxDy) maxDy = planet.dy;
            if (planet.dy < minDy) minDy = planet.dy;

            if (planet.dz > maxDz) maxDz = planet.dz;
            if (planet.dz < minDz) minDz = planet.dz;
        }
        dx = (maxDx + minDx) / 2;
        dy = (maxDy + minDy) / 2;
        dz = (maxDz + minDz) / 2;
        x = -dx; y = -dy; z = -dz;
    }

    static double AZ = 90, AY = 0;


    private double[] getPosOf(long timeStamp, double distance){
        distance+=1;
        return calc(0, 0, 90+(distance*60), 0,0, 0, wrapDegrees360(
                timeStamp/(distance * distance * 300)
        ), AY, AZ);
    }


    public boolean selectMain(GalName name){
//        while (skip>0){
//            name = name.getParent();
//            --skip;
//        }
        do {
            for (int id = 0; id < planets.length; id++) {
                VPlanet planet = planets[id];
                if (planet == null) continue;
                if (planet.obj.name.equals(name)) {
                    planet.selection = 1;
                    msel = id;
                    return true;
                }
            }
            name = name.getParent();
        } while (name != null);
        msel = -1;
        return false;
    }


    public static double wrapDegrees360(double f){
        if(f>360) return f%360;
        return f<0?360 + f%360f:f;
    }


    public SSModel(GuiStarMap guiStarMap, VPlanet... planets) {
        this.guiStarMap = guiStarMap;
        this.planets = planets;
        dumpPos = new double[planets.length][];
    }


    public void translate() {
        //GlStateManager.translate(dx, dy, dz);
    }


    public void draw() {
        for (VPlanet planet : planets) {
            if(planet==null)continue;
            guiStarMap.drawObj(x, y, z, planet);
        }
        if(canDrawLine()){
            //Tessellator tsr = Tessellator.getInstance();

        }
    }


    public void drawNames(){
        for (int i=0;i<planets.length;i++) {
            GlStateManager.pushMatrix();
            VPlanet planet = planets[i];
            if(planet==null)continue;

            double[] ds = {x + planet.dx, y + planet.dy, z + planet.dz};
            ds  = calc(ds, zp, guiStarMap.manager.curAy, guiStarMap.manager.curAx, 0);

            float scale = (planet.selection==2||planet.selection==3)?planet.scale*1.5f:planet.scale;

            float fx2 = (float)(ds[0] - (GuiStarMap.fontRenderer.getStringWidth(planet.obj.name.getSelfName()) / 2));
            float fy2 = (float)(ds[1] - 23*scale) -5;

            if(planet.obj.type()==-1)
                guiStarMap.drawBlur((float) (ds[0]), (float) (ds[1]), (float) z, scale, planet.obj.textureId);

            GlStateManager.translate(0, 0, ds[2]+1);
            drawStr(planet.obj.name.getSelfName(), fx2, fy2);

            GlStateManager.popMatrix();
        }
    }


    private int drawStr(String str, float x, float y){
        return GuiStarMap.fontRenderer.drawString(str, x, y, 0xFFFFFF, false);
    }


    private String translate(String str, Object... args){
        return I18n.translateToLocalFormatted("starmap."+str, args);
    }


    //TODO remove args and fix blur layer
    private final ETextRenderer.EIcoLines sellInf = new ETextRenderer.EIcoLines();
    public void drawSellInfo(float mx, float my){
        if(selection==-1)return;
        VPlanet planet = planets[selection];
        if(planet==null)return;
        GlStateManager.pushMatrix();
        double[] ds = {x + planet.dx, y + planet.dy, z + planet.dz};
        ds  = calc(ds, zp, guiStarMap.manager.curAy, guiStarMap.manager.curAx, 0);

        float shift = 30*planet.scale*1.4f;
        float fx2 = (float)(ds[0] + shift     );
        float fy2 = (float)(ds[1] - (shift/2) );

        sellInf.clear();
        sellInf.add(-1, planet.obj.name.getSelfName());

        boolean canFlyTo = MBridge.CAN_FLY_TO.isSucces(planet.obj.name);


        if(planet.obj.type()==-1){
            if(planet.obj != guiStarMap.currentStar && guiStarMap.currentStar != null){
                SolarMap.Star star1 = (SolarMap.Star) planet.obj;
                int dx = star1.xPos - guiStarMap.currentStar.xPos;
                int dy = star1.yPos - guiStarMap.currentStar.yPos;
                int dz = star1.zPos - guiStarMap.currentStar.zPos;

                float range = ((int)(Math.sqrt(dx * dx + dy * dy + dz * dz)*10f))/10f;

                if(canFlyTo){
                    sellInf.add(10, translate("range_pc_near", range));
                } else {
                    sellInf.add(10, translate("range_pc", range));
                }

            }
        }

        int cp = planet.obj.getCountPlanets();
        if(cp>0){
            sellInf.add(planet.obj.type()==-1?3:4, translate("orbit", cp));
            //drawStr(planet.obj.type()==-1?3:4, translate("orbit", cp), fx2,fy2+cursor);
            //cursor+=10;
        }
        cp = StarTextureRepo.stationFraction(planet.obj.textureId);
        if(cp!=-1){
            String type = null;
            switch (cp){
                case 1: cp = 11;type = "neytral" ;break;
                case 2: cp = 14;type = "piracy"  ;break;
                case 3: cp = 13;type = "voids"   ;break;
                case 4: cp = 12;type = "imperia" ;break;
            }
            sellInf.add(cp, translate("fract."+type));
            //drawStr(cp, translate("fract."+type), fx2,fy2+cursor);
            //cursor+=10;
        }

        if(planet.obj.type()==0) {//is planet
            int sq = CommonCalculator.blocksOf(planet.obj.scale);
            sellInf.add(2, translate("square", sq*sq));
            //drawStr(2, translate("square", sq*sq), fx2,fy2+cursor);
            //cursor+=10;

            sellInf.add(planet.obj.bool1?0:8, translate("oxygen."+planet.obj.bool1));

            //drawStr(planet.obj.bool1?0:8, translate("oxygen."+planet.obj.bool1), fx2,fy2+cursor);
            //cursor+=10;
            boolean hasWater = StarTextureRepo.planetHasWater(planet.obj.textureId);
            sellInf.add(hasWater?1:9, translate("water."+hasWater));
            //drawStr(hasWater?1:9, translate("water."+hasWater), fx2,fy2+cursor);
            //cursor+=10;


        } else {
            //drawStr("oxygen: "+planet1.b1,  fx2+10, fy2);
        }

        goToType = -1;
        if(canFlyTo){
            if(msel==selection){
                goToType = 1;
            }else if(planet.obj.getCountPlanets()>0){
                if(selection==0){
                    if(planet.obj.type()==-1){
                        goToType = -1;
                    } else {
                        goToType = 0;
                    }
                } else {
                    goToType = 2;
                }
            } else {
                goToType = 0;
            }
        }

        if(goToType >= 0){
            ETextRenderer.ICoLine line = new ETextRenderer.ICoLine();
            String text;
            switch (goToType){
                case 1:  text = "you_here";break;
                case 2:  text = "open_map";break;
                default: text = "goto"    ;break;
            }
            line.text = translate(text);

            float width = line.width();
            width = sellInf.get(0).width = Math.max(sellInf.width()+1, width+1);
            sellInf.setWidth(width);

            guiStarMap.drawEt(sellInf, fx2, fy2, 0xFFFFFF);

            float size = sellInf.height()+12;

            sellInf.clear();
            sellInf.add(-1, width, line.text);
            sellInf.setWidth(width);

            //is hovered
            if(
                    isBetween(my, fy2+size-8+sellInf.height(), fy2+size-17) &&
                    isBetween(mx, fx2+width+15, fx2+8)
            ){
                guiStarMap.drawEt(sellInf, fx2, fy2+size,
                        0xFFFFFF, 0x70_11_11_11, 0xD0_70_70_FF, 0xD0_22_22_66);
            } else {
                goToType = -1;
                guiStarMap.drawEt(sellInf, fx2, fy2+size,
                        0xFFFFFF, 0x70_11_11_11, 0x50_DD_40_40, 0x50_22_00_00);
            }
        } else {
            float width = sellInf.get(0).width = sellInf.width()+1;
            sellInf.setWidth(width);
            guiStarMap.drawEt(sellInf, fx2, fy2, 0xFFFFFF);
        }

        GlStateManager.popMatrix();
    }

    private int goToType = -1;

    public boolean isGoToHovered(){
        return goToType >= 0;
    }


    public boolean isCanGoto(){
        if(goToType!=0)return false;
        VPlanet sel = getSelected(false);
        if(sel==null || selection == msel)return false;
        return sel.obj.type()>-1;
    }


    public double[] getPos(){
        return new double[]{x, y, z};
    }

    private final double[] zp = {0, 0, 0};
    public void calcPositions(double ax, double ay, double az) {
        updatePlanetsIfNeed();
        x = ax-dx; y = ay-dy; z = az-dz;

        for (int i = 0; i < planets.length; i++) {
            VPlanet planet = planets[i];
            double[] ds = {x + planet.dx, y + planet.dy, z + planet.dz};
            dumpPos[i] = calc(ds, zp, guiStarMap.manager.angleY, guiStarMap.manager.angleX, 0);
        }
    }


    public double[] get1pVec(){
        return calc(new double[]{1, 1, 1}, zp, guiStarMap.manager.angleY, guiStarMap.manager.angleX, 0);
    }


    public VPlanet sellect(float mx, float my) {
        int result = -1;
        for (int i = 0; i < planets.length; i++) {
            planets[i].selection = 0;
            double[] pos = dumpPos[i];
            double xp = pos[0], yp = pos[1];
            float range = 20*planets[i].scale;
            if (isBetween(mx, xp + range, xp - range) && isBetween(my, yp + range, yp - range)) {
                if (result == -1 || dumpPos[result][2] < pos[2]) {
                    result = i;
                }
            }
        }
        selection = result;
        if(msel != -1){
            planets[msel].selection = 1;
        }
        if (selection != -1) {
            planets[selection].selection = (byte) (planets[selection].selection==1?3:2);
            return planets[selection];
        }
        return null;
    }


    private static boolean isBetween(double val, double max, double min) {
        return val < max && val > min || val > max && val < min;
    }


    public boolean canDrawLine(){
        return selection != -1 && msel != -1;
    }


    public VPlanet getSelected(boolean main){
        int selection = main?msel:this.selection;
        return selection==-1?null:planets[selection];
    }


//    public SolarMap.Planet getSelectedPlanet(){
//        if(planets0==null || selection<0 || selection>=planets0.length)return null;
//        return planets0[selection];
//    }


    public double[] getSelectedPos(){
        return selection==-1?null:dumpPos[selection];
    }
}