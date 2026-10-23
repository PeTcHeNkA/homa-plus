package plus.item.stat;

import plus.Main;
import plus.util.AutoReloadable;
import plus.util.cfg.Config;
import plus.util.cfg.Sector;
import java.lang.reflect.Field;
import java.util.Random;


public class StatGenerator {

    protected static final Random r = new Random();
    private static double math(double basic, double lvlable, int lvl){ return basic + lvlable*lvl + r.nextFloat()*lvlable; }

    private final StatLabel1 S;
    public StatGenerator(StatLabel1 l){
        S = l;
    }
    public double genW(int lvl){ return lvl>0?math(S.BASIC.WEAPON, S.LVLABLE.WEAPON, lvl):0; }
    public double genA(int lvl){ return lvl>0?math(S.BASIC.ARTIFACT, S.LVLABLE.ARTIFACT, lvl):0; }


    public static shieldMather MATHER_SHIED;
    public static attackMather MATHER_ATTACK;
    public static critRateMather MATHER_CRIT_RATE;
    public static critPowerMather MATHER_CRIT_POWER;
    public static energyMather MATHER_ENERGY;

    public static StatGenerator DAMAGE;
    public static StatGenerator DAMAGE_PERCENT;
    public static StatGenerator CRIT_DAMAGE_PERCENT;
    public static StatGenerator CRIT_RATE_PERCENT;
    public static StatGenerator SHIELD;
    public static StatGenerator SHIELD_PERCENT;
    public static StatGenerator ENERGY;

    public static void define(){
        new AutoReloadable() {
            final Config c = new Config(Main.instance, "STAT_GENERATOR.yml");
            @Override
            public void reload() {
                c.reload();
                fillValues(c);
                MATHER_SHIED = new shieldMather(c, "SHIELD");
                MATHER_ATTACK = new attackMather(c, "ATTACK");
                MATHER_CRIT_RATE = new critRateMather(c, "CRIT_RATE");
                MATHER_CRIT_POWER = new critPowerMather(c, "CRIT_POWER");
                MATHER_ENERGY = new energyMather(c, "ENERGY");
            }
        }.reload();
    }

    public static void fillValues(Config s){
        for(Field f:StatGenerator.class.getDeclaredFields()){
            if(f.getType().getSimpleName().equals("StatGenerator")){
                int m = f.getModifiers();
                if(m!=9)return;
                String name = f.getName();
                try {
                    if(name.endsWith("_PERCENT")){
                        f.set(StatGenerator.class, new StatGenerator(new StatLabel1(s, name, 0.01)));
                    } else {
                        f.set(StatGenerator.class, new StatGenerator(new StatLabel1(s, f.getName())));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static class shieldMather extends mather{
        public shieldMather(Sector s, String name) {
            super(s, name);
        }

        public double math(int points, double startShield,
                           double artifactShieldBoost, double weaponShieldBoost,
                           double additionalShield, double weaponShield){
            return startShield + (startShield*FACTOR1*points)*(1+(artifactShieldBoost+weaponShieldBoost)*0.01)
                    + additionalShield + weaponShield;
        }
    }

    public static class attackMather extends mather{
        public attackMather(Sector s, String name) {
            super(s, name);
        }

        public double math(int points, double startAttack,
                           double additionalAttack, double weaponAttack,
                           double attackBoost, double weaponAttackBoost){
            double sumAttack = startAttack + weaponAttack;
            return sumAttack + (sumAttack*FACTOR1*points) * (1+attackBoost+weaponAttackBoost)+additionalAttack;
        }
    }

    public static class critRateMather extends mather{
        public critRateMather(Sector s, String name) {
            super(s, name);
        }

        public double math(int points, double startCriticalRate,
                           double additionalCriticalRate, double weaponCritRateBoost){
            return ((additionalCriticalRate+weaponCritRateBoost)*0.01) + (startCriticalRate*(1+(FACTOR1*points)));
        }
    }

    public static class critPowerMather extends mather{
        public critPowerMather(Sector s, String name) {
            super(s, name);
        }

        public double math(int points, double startCriticalPower,
                           double additionalCriticalPower, double weaponCritBoost){
            return additionalCriticalPower+weaponCritBoost+(startCriticalPower*(1+(FACTOR1*points)));
        }
    }

    public static class energyMather extends mather{
        public energyMather(Sector s, String name) {
            super(s, name);
        }
        public double math(int points, double startEnergyAmount,
                           double additionalEnergyAmount, double weaponAdditionalEnergy){
            return startEnergyAmount + (startEnergyAmount * FACTOR1 * points) + additionalEnergyAmount + weaponAdditionalEnergy;
        }
    }
}
abstract class mather{
    public final double FACTOR1;

    public mather(Sector s, String name) {
        FACTOR1 = s.getOrSetDouble("VALCIRIA_MATHER_"+name+"_POINTS_FACTOR", 1)*0.01;
    }
}