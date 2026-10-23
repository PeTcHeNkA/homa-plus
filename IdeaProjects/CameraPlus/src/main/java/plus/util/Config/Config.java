package plus.util.Config;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Config implements ConfigurationSection, Sector {

    private SimpleSector c;
    public Config(Plugin p, String name){
        if(p == null)
            c = new CoreSector(name);
        else
            c = new PluginSector(p, name);
    }
    public Config(String name){
        c = new CoreSector(name);
    }

    public void Reload() {
        c = c.reloadAsFile();
    }
    public void saveChanges(){
        c.saveChanges();
    }

    public String getColoredString(String patch){
        return c.getColoredString(patch);
    }
    public String getColoredString(String patch, String def){
        return c.getColoredString(patch, def);
    }

    @Override
    public Set<String> getKeys(boolean bl) {
        return c.getKeys(bl);
    }

    @Override
    public Map<String, Object> getValues(boolean bl) {
        return c.getValues(bl);
    }

    @Override
    public boolean contains(String patch) {
        return c.contains(patch);
    }

    @Override
    public boolean contains(String patch, boolean bl) {
        return c.contains(patch, bl);
    }

    @Override
    public boolean isSet(String string) {
        return c.isSet(string);
    }

    @Override
    public String getCurrentPath() {
        return c.getCurrentPath();
    }

    @Override
    public String getName() {
        return c.getName();
    }

    @Override
    public Configuration getRoot() {
        return c.getRoot();
    }

    @Override
    public ConfigurationSection getParent() {
        return c.getParent();
    }

    @Override
    public Object get(String string) {
        return c.get(string);
    }

    @Override
    public Object get(String string, Object object) {
        return c.get(string, object);
    }

    @Override
    public void set(String string, Object object) {
        c.set(string, object);
    }

    @Override
    public ConfigurationSection createSection(String string) {
        return c.createSection(string);
    }

    @Override
    public ConfigurationSection createSection(String string, Map<?, ?> map) {
        return c.createSection(string, map);
    }

    @Override
    public String getString(String string) {
        return c.getString(string);
    }

    @Override
    public String getString(String patch, String def) {
        return c.getString(patch, def);
    }

    @Override
    public String getOrSetIfExistString(String patch, String def) {
        String r = c.getOrSetIfExistString(patch, def);
        c.saveChanges();
        //reloadAsFile();
        return r;
    }

    @Override
    public int getOrSetIfExistInt(String patch, int def){
        int r = c.getOrSetIfExistInt(patch, def);
        c.saveChanges();
        return r;
    }

    @Override
    public boolean getOrSetIfExistBoolean(String patch, boolean def) {
        boolean r = c.getOrSetIfExistBoolean(patch, def);
        c.saveChanges();
        return r;
    }

    @Override
    public List<String> getOrCreateStringList(String patch, List<String> def){
        List<String> result = c.getOrCreateStringList(patch, def);
        c.saveChanges();
        return result;
    }

    @Override
    public boolean isString(String patch) {
        return c.isString(patch);
    }

    @Override
    public int getInt(String string) {
        return c.getInt(string);
    }

    @Override
    public int getInt(String string, int i) {
        return c.getInt(string, i);
    }

    @Override
    public boolean isInt(String string) {
        return c.isInt(string);
    }

    @Override
    public boolean getBoolean(String string) {
        return c.getBoolean(string);
    }

    @Override
    public boolean getBoolean(String string, boolean bl) {
        return c.getBoolean(string, bl);
    }

    @Override
    public boolean isBoolean(String patch) {
        return c.isBoolean(patch);
    }

    @Override
    public double getDouble(String patch) {
        return c.getDouble(patch);
    }

    @Override
    public double getDouble(String string, double d) {
        return c.getDouble(string, d);
    }

    @Override
    public boolean isDouble(String string) {
        return c.isDouble(string);
    }

    @Override
    public long getLong(String string) {
        return c.getLong(string);
    }

    @Override
    public long getLong(String string, long l) {
        return c.getLong(string, l);
    }

    @Override
    public boolean isLong(String string) {
        return c.isLong(string);
    }

    @Override
    public List<?> getList(String string) {
        return c.getList(string);
    }

    @Override
    public List<?> getList(String string, List<?> list) {
        return c.getList(string, list);
    }

    @Override
    public boolean isList(String string) {
        return c.isList(string);
    }

    @Override
    public List<String> getStringList(String patch) {
        return c.getStringList(patch);
    }

//    @Override
//    public List<String> getOrCreateStringList(String patch, List<String> def){
//        if(!c.isList(patch) || !c.contains(patch)){
//            c.set(patch, def);
//        }
//        return def;
//    }



    @Override
    public List<Integer> getIntegerList(String string) {
        return c.getIntegerList(string);
    }

    @Override
    public List<Boolean> getBooleanList(String string) {
        return c.getBooleanList(string);
    }

    @Override
    public List<Double> getDoubleList(String string) {
        return c.getDoubleList(string);
    }

    @Override
    public List<Float> getFloatList(String string) {
        return c.getFloatList(string);
    }

    @Override
    public List<Long> getLongList(String string) {
        return c.getLongList(string);
    }

    @Override
    public List<Byte> getByteList(String string) {
        return getByteList(string);
    }

    @Override
    public List<Character> getCharacterList(String string) {
        return c.getCharacterList(string);
    }

    @Override
    public List<Short> getShortList(String string) {
        return c.getShortList(string);
    }

    @Override
    public List<Map<?, ?>> getMapList(String string) {
        return c.getMapList(string);
    }

    @Override
    public <T> T getObject(String string, Class<T> class_) {
        return c.getObject(string, class_);
    }

    @Override
    public <T> T getObject(String string, Class<T> class_, T object) {
        return c.getObject(string, class_, object);
    }

    @Override
    public <T extends ConfigurationSerializable> T getSerializable(String string, Class<T> class_) {
        return c.getSerializable(string, class_);
    }

    @Override
    public <T extends ConfigurationSerializable> T getSerializable(String string,  Class<T> class_, T configurationSerializable) {
        return c.getSerializable(string, class_, configurationSerializable);
    }

    @Override
    public Vector getVector(String string) {
        return c.getVector(string);
    }

    @Override
    public Vector getVector(String string, Vector vector) {
        return c.getVector(string, vector);
    }

    @Override
    public boolean isVector(String string) {
        return c.isVector(string);
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String string) {
        return c.getOfflinePlayer(string);
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String string, OfflinePlayer offlinePlayer) {
        return c.getOfflinePlayer(string, offlinePlayer);
    }

    @Override
    public boolean isOfflinePlayer(String string) {
        return c.isOfflinePlayer(string);
    }

    @Override
    public ItemStack getItemStack(String string) {
        return c.getItemStack(string);
    }

    @Override
    public ItemStack getItemStack(String string, ItemStack itemStack) {
        return c.getItemStack(string);
    }

    @Override
    public boolean isItemStack(String string) {
        return c.isItemStack(string);
    }

    @Override
    public Color getColor(String string) {
        return c.getColor(string);
    }

    @Override
    public Color getColor(String string, Color color) {
        return c.getColor(string, color);
    }

    @Override
    public boolean isColor(String string) {
        return c.isColor(string);
    }

    @Override
    public Location getLocation(String string) {
        return c.getLocation(string);
    }

    @Override
    public Location getLocation(String string, Location location) {
        return c.getLocation(string, location);
    }

    @Override
    public boolean isLocation(String string) {
        return c.isLocation(string);
    }

    @Override
    public ConfigurationSection getConfigurationSection(String string) {
        return c.getConfigurationSection(string);
    }

    @Override
    public boolean isConfigurationSection(String string) {
        return c.isConfigurationSection(string);
    }

    @Override
    public ConfigurationSection getDefaultSection() {
        return c.getDefaultSection();
    }

    @Override
    public void addDefault(String string, Object object) {
        c.addDefault(string, object);
    }


}
