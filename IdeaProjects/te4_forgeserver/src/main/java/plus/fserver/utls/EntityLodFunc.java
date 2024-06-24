package plus.fserver.utls;

import plus.fserver.ElcaMobs;
import plus.tson.TsonObj;
import static plus.tson.ext.TsonAccessUtils.*;


public final class EntityLodFunc {
    public final double near, low;

    public EntityLodFunc(TsonObj obj, double near, double low){
        this.near = param(obj, near, "0","near");
        this.low  = param(obj, low,  "1","low" );
    }


    private static double param(TsonObj obj, double def, String... keys) {
        double pre = getR(obj, def, keys);
        return pre * pre * 3;
    }


    public EntityLodFunc(TsonObj obj){
        near = param(obj, ElcaMobs.GLOBAL.near, "0","near");
        low  = param(obj, ElcaMobs.GLOBAL.low,  "1","low" );
    }


    public EntityLodFunc(double near, double lim2) {
        this.near = near;
        this.low = lim2;
    }


    public int lodLvl(double d) {
        if(d < near) return 0;
        return d < low ? 1 : 2;
    }
}