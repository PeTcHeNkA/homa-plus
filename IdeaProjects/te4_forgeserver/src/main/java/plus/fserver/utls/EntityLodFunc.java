package plus.fserver.utls;

import plus.fserver.ElcaMobs;
import plus.fserver.api.Keep;
import plus.tson.TsonObj;
import static plus.tson.ext.TsonAccessUtils.*;


@Keep
public final class EntityLodFunc {
    public static final int NEAR_MASK = 0;
    public static final int LOW1_MASK = 1;
    public static final int LOW2_MASK = 3;
    public static final int LOW3_MASK = 7;
    public final double near, low, lowest;

    public EntityLodFunc(TsonObj obj, double near, double low){
        this.near   = param(obj, near, "0","near");
        this.low    = param(obj, low,  "1","low" );
        this.lowest = param(obj, low+(low-near), "lowest");
    }


    private static double param(TsonObj obj, double def, String... keys) {
        double pre = getR(obj, def, keys);
        return pre * pre * 3;
    }


    public EntityLodFunc(TsonObj obj){
        near = param(obj, ElcaMobs.GLOBAL.near, "0","near");
        low  = param(obj, ElcaMobs.GLOBAL.low,  "1","low" );
        lowest = param(obj, low+(low-near), "lowest");
    }


    public EntityLodFunc(double near, double lim2) {
        this.near = near;
        this.low = lim2;
        this.lowest = low+(low-near);
    }


    public int lodLvl(double d) {
        if(d < near)  return NEAR_MASK;
        if(d < low)   return LOW1_MASK;
        if(d < lowest)return LOW2_MASK;
        return LOW3_MASK;
    }
}