package plus.mpck;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


/**
 * MMO Placeholders kit (As PAPI Extension)
 * A set of useful placeholders for PVP/PVE
 * <br><br>
 * mpc_cur_health - Current health
 * <br>
 * mpc_max_health - Max. Health
 * <br>
 * mpc_damage     - Current damage dealt
 * <br>
 * mpc_armor      - Current "booking" (0-20)
 * <br>
 * mpc_food_lvl   - Current value of food
 *
 * @author HomaPlus
 */
public class MPlaceholders extends PlaceholderExpansion {
    /**
     * Fix 1.16 -> 1.20 support with 1.21
     */
    static PotionEffectType STRENGTH;

    static {
        try {
            for (PotionEffectType type:PotionEffectType.values())
                if(type.getKey().getKey().equals("strength")){
                    STRENGTH = type;
                    break;
                }
            if(STRENGTH == null)
                throw new RuntimeException("Effect [strength] not founded. Core maybe corrupted?");
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public String getIdentifier() {
        return "mpc";
    }


    @Override
    public final String getAuthor() {
        return "HomaPlus";
    }


    @Override
    public final String getVersion() {
        return "1.1a";
    }


    /**
     * Process placeholders
     */
    @Override
    public String onPlaceholderRequest(Player player, String params) {
        switch (params){
            case "health"    : return String.valueOf((int)player.getHealth());
            case "max_health": return String.valueOf((int)player.getMaxHealth());
            case "armor"     : return String.valueOf(armor(player));
            case "food_lvl"  : return String.valueOf(player.getFoodLevel());
            case "damage"    : return String.valueOf(damage(player));
        }
        return null;
    }


    /**
     * Calc current armor (0-20)
     */
    static int armor(Player player){
        AttributeInstance attr;
        if((attr = player.getAttribute(Attribute.GENERIC_ARMOR)) == null)return 0;
        return clamp((int)attr.getValue(), 0, 20);
    }


    /**
     * Calc current player damage
     */
    static int damage(Player player) {
        AttributeInstance attr;
        double curDmg = 1;
        if((attr = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)) != null)
            curDmg = attr.getValue();

        PotionEffect eff;

        if(STRENGTH != null && (eff = player.getPotionEffect(STRENGTH)) != null)
            curDmg += (3d * eff.getAmplifier());

        return (int)curDmg;
    }


    static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }
}