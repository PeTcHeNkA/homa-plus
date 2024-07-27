package plus.space.client.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.space.client.SolarMap;


@SideOnly(Side.CLIENT)
public final class VPlanet {
    double dx = 0, dy = 0, dz = 0;
    byte selection;
    public final SolarMap.MapObj obj;
    public final float scale;

    public VPlanet(SolarMap.MapObj obj) {
        this.obj = obj;
        this.scale = obj.scale;
    }

    public VPlanet(SolarMap.MapObj obj, float scale) {
        this.obj = obj;
        this.scale = scale;
    }


    public void setPos(double dx, double dy, double dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }


    public boolean sellected(){
        return selection==2||selection==3;
    }
}