package plus.fserver.api.anim;

import net.minecraft.entity.player.EntityPlayerMP;
import plus.tson.TsonFunc;


public interface IRelativeFunc extends TsonFunc {
    default Object call(Object... args) {
        call((Double) args[0], (Double) args[1], (Double) args[2], (EntityPlayerMP)args[3]);
        return null;
    }
    void call(double x, double y, double z, EntityPlayerMP player);


    /**
     * Try merge functions to one
     */
    static TsonFunc merge(TsonFunc... funcs){
        if(funcs.length == 0)throw new IllegalArgumentException();
        if(funcs.length == 1)return funcs[0];
        return new MergedFunc(funcs);
    }
}


class MergedFunc implements IRelativeFunc{
    private final TsonFunc[] funcs;

    MergedFunc(TsonFunc[] funcs) {
        this.funcs = funcs;
    }


    @Override
    public Object call(Object arg) {
        Object last = null;
        for (TsonFunc func:funcs) last = func.call(arg);
        return last;
    }


    @Override
    public Object call() {
        Object last = null;
        for (TsonFunc func:funcs) last = func.call();
        return last;
    }


    @Override
    public Object call(Object... args) {
        Object last = null;
        for (TsonFunc func:funcs) last = func.call();
        return last;
    }


    @Override
    public void call(double x, double y, double z, EntityPlayerMP player) {
        for (TsonFunc func:funcs){
            if(func instanceof IRelativeFunc){
                ((IRelativeFunc) func).call(x, y, z, player);
            } else {
                func.call(player);
            }
        }
    }
}