package plus.util.Config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public interface Sector extends ConfigurationSection {
    void saveChanges();

    String getColoredString(String path);

    String getColoredString(String path, String def);

    String getString(String path);

    String getString(String path, String def);

    String getOrSetIfExistString(String patch, String def);

    boolean getOrSetIfExistBoolean(String patch, boolean def);

    List<String> getOrCreateStringList(String patch, List<String> def);

    int getOrSetIfExistInt(String patch, int def);

}
