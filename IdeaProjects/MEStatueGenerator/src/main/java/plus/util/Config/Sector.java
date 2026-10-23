package plus.util.Config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public interface Sector extends ConfigurationSection  {
    void saveChanges();

    String getColoredString(String path);

    String getColoredString(String path, String def);

    String getString(String path);

    String getString(String path, String def);

    String getOrSetString(String patch, String def);

    String getOrSetString(String patch, String def, boolean Colored);

    boolean getOrSetBoolean(String patch, boolean def);

    List<String> getOrSetStringList(String patch, List<String> def);

    int getOrSetInt(String patch, int def);

    void createBackUp();

    double getOrSetDouble(String patch, double def);

}
