package plus.valcirias.avatar;

import org.bukkit.configuration.ConfigurationSection;
import plus.util.ConfigHelper;
import java.util.HashMap;
import java.util.Set;


public class AvatarFloader {

    private static final HashMap<String, AvatarSet> avavarSets= new HashMap<>();
    public static Avatar startAvatar;

    public static void Initialize(){
        ConfigurationSection section = ConfigHelper.c.getConfigurationSection("avatarsets");
        if(section==null)return;
        for(String name:section.getKeys(false)){
            if(name.charAt(name.length()-1)=='=')continue;
            avavarSets.put(name,
                    new AvatarSet(
                            name,
                            ConfigHelper.getConfigStrValue("avatarsets."+name+".chars"),
                            ConfigHelper.getConfigStrValue("avatarsets."+name+".cmd"),
                            ConfigHelper.getConfigStrValue("avatarsets."+name+".premission")
                    )
            );
            System.out.printf("| Loaded avatar set [%s], size = %s%n", name, avavarSets.get(name).getCount());
        }
        startAvatar = new StartAvatar(
                ConfigHelper.c.getOrSetString("avatarsets.default=.char", "n").charAt(0),
                ConfigHelper.c.getOrSetInt("avatarsets.default=.cmd", 0)
        );
    }


    public static AvatarSet getAvatarSet(String name){
        return avavarSets.get(name);
    }


    public static Set<String> getAvatarSetNames(){
        return avavarSets.keySet();
    }
}