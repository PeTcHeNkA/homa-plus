package plus.common.mashine.item;

import net.minecraft.item.ItemStack;


public class ReactCore extends ReactComponent {
    public static final ReactCore CORE_L1 = new ReactCore("react_core_l1", 1,14875);
    public static final ReactCore CORE_L2 = new ReactCore("react_core_l2", 2,24480);
    public static final ReactCore CORE_L3 = new ReactCore("react_core_l3", 3,33320);
    public static final ReactCore CORE_L4 = new ReactCore("react_core_l4", 4,42840);

    public static final Booster BOOSTER_L1 = new Booster("react_boost_l1", 1, 17500);
    public static final Booster BOOSTER_L2 = new Booster("react_boost_l2", 2, 28800);

    protected ReactCore(String name, int lvl, int usages) {
        super(name, lvl, usages);
    }


    public boolean canInstall(ItemStack item) {
        if (isReactComponent(item)) {
            return level >= ((ReactComponent) item.getItem()).level;
        }
        return false;
    }


    public TYPE getType(){
        return TYPE.CORE;
    }
}