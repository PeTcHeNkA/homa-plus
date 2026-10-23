package plus.item.stat;

import plus.util.cfg.Sector;


public class StatLabel1 {

    public final StatLabel BASIC;
    public final StatLabel LVLABLE;

    public StatLabel1(Sector s, String name, double booster) {
        BASIC = new StatLabel(s, "BASIC_"+name, booster);
        LVLABLE = new StatLabel(s, "LVLABLE_"+name, booster);
    }


    public StatLabel1(Sector s, String name){
        this(s, name, 1);
    }
}