package plus.item.stat;

import plus.util.cfg.Sector;


public class StatLabel{

    public final double ARTIFACT;
    public final double WEAPON;

    public StatLabel(Sector s, String name, double booster){
        String path = name.toUpperCase();
        ARTIFACT = s.getOrSetDouble(path+"_ARTIFACT", 1)*booster;
        WEAPON = s.getOrSetDouble(path+"_WEAPON", 1)*booster;
    }

    public StatLabel(Sector s, String name){
        this(s, name, 1);
    }
}