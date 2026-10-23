package plus.valcirias.data;

import org.bukkit.Material;
import plus.Main;
import plus.item.BaseInfo;
import plus.item.BoostStats;
import plus.util.AutoReloadable;
import plus.util.ConfigHelper;
import plus.util.Reloadable;
import plus.util.cfg.Config;
import plus.util.cfg.Sector;
import java.lang.reflect.Field;
import java.util.HashMap;


public class ClassStats {

    private static final HashMap<String, ClassStats> cache = new HashMap<>();
    private static Config c;

    public static int MAX_BATTLE_PASS;
    public static int CD_RECOVERY_BATTLE_PASS;
    public static int CD_PERMISSIBLE_ERROR;

    public int ATTACK_POINTS;
    public int SHIELD_POINTS;
    public int ENERGY_POINTS;
    public int CRITICAL_POINTS;
    public int START_ENERGY_AMOUNT;
    public int START_SHIELD;
    public int NORMAL_CD;
    public int HEIGHT_CD;
    public int NORMAL_PRICE;
    public int HEIGHT_PRICE;
    public double START_ATTACK;
    public double START_CRIT_RATE;
    public double START_CRIT_POWER;
    public final String LOCAL_NAME;
    public final StartItemKit KIT;

    public ClassStats(String name){
        String path = "VALCIRIAS."+name.toUpperCase();
        fillValues(c, path);

        BaseInfo info;
        BoostStats stats = null;
        String rawInfo = c.getOrSetString(path + ".WEAPON", "main&0&1&1=");
        if(rawInfo.contains("=")){
            String[] s2 = rawInfo.split("=");
            info = new BaseInfo(s2[0]);
            if(s2.length>1 && !s2[1].isEmpty())stats = new BoostStats(s2[1]);
        } else {
            info = new BaseInfo(rawInfo);
        }

        KIT = new StartItemKit(
                Main.generator.buildWeapon(info, stats,
                        c.getOrSetBoolean(path + ".WEAPON_IS_FRAME", true)
                ),
                ConfigHelper.generateMenuItem(c, path + ".SKILL_NORMAL", Material.ARROW),
                ConfigHelper.generateMenuItem(c, path + ".SKILL_HEIGHT", Material.ENDER_PEARL)
        );
        LOCAL_NAME = c.getOrSetString(path+".LOCAL_NAME", name, true);
    }


    protected static void fillStatic(Sector s){
        for(Field f: ClassStats.class.getDeclaredFields()){
            int m = f.getModifiers();
            if(m!=9)continue;
            String name = f.getName();
            try {
                String path2 =  "GL_" + name.toUpperCase();
                if(f.getType().getSimpleName().equals("double")){
                    f.set(ClassStats.class, s.getOrSetDouble(path2, 1));
                } else if(f.getType().getSimpleName().equals("int")){
                    f.set(ClassStats.class, s.getOrSetInt(path2, 1));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


    private void fillValues(Sector s, String path){
        for(Field f: this.getClass().getDeclaredFields()){
            int m = f.getModifiers();
            if(m==9)continue;
            String name = f.getName();
            try {
                String path2 = path + "." + name.toUpperCase();
                if(f.getType().getSimpleName().equals("double")){
                    f.set(this, s.getOrSetDouble(path2, 0));
                } else if(f.getType().getSimpleName().equals("int")){
                    f.set(this, s.getOrSetInt(path2, 0));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


    public static void define(){
        if(c==null){
            c = new Config(Main.instance, "class_stats.yml");
            fillStatic(c);
            Reloadable.add(new AutoReloadable() {
                @Override
                public void reload() {
                    cache.clear();
                    fillStatic(c);
                    c.reload();
                }
            });
        }
    }


    public static ClassStats getOf(String className){
        ClassStats stats = cache.get(className);
        if(stats==null){
            stats = new ClassStats(className);
            cache.put(className, stats);
        }
        return stats;
    }


    public static double getClassStatValue(String className, String var, double d){
        String path = "VALCIRIAS."+(className+"."+var).toUpperCase();
        return c.getOrSetDouble(path, d);
    }

    public static int getClassStatValue(String className, String var, int i){
        String path = "VALCIRIAS."+(className+"."+var).toUpperCase();
        return c.getOrSetInt(path, i);
    }
}