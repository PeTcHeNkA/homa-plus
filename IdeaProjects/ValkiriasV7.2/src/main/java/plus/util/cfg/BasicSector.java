package plus.util.cfg;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;


public abstract class BasicSector extends YamlConfiguration implements SimpleSector {

    protected String name;
    protected File file;

    @Override
    public void saveChanges(){
        try {
            save(file);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void createBackUp(){
        try {
            int n = new Random(System.currentTimeMillis()).nextInt(0, 999);
            String path = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - file.getName().length())+"backups";
            File dir = new File(path);
            if(!dir.exists() || !dir.isDirectory())dir.mkdir();

            path = path+File.separator + name.replace(".", "_"+n + ".");

            File f2 = new File(path);
            f2.createNewFile();
            save(f2);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public SimpleSector reloadAsFile(){
        return new CoreSector(name);
    }


    @Override
    public String getFileName(){
        return name;
    }


    @Override
    public String getColoredString(String path) {
        return super.getString(path, "").replace('&', '\u00A7');
    }


    @Override
    public String getColoredString(String path, String def) {
        return parseColor(super.getString(path, def));
    }


    @Override
    public String getString(String path){
        return super.getString(path);
    }


    @Override
    public String getString(String path, String def){
        return super.getString(path, def);
    }


    @Override
    public String getOrSetString(String patch, String def) {
        String r = super.getString(patch);
        if(r==null){
            super.set(patch, def);
            return def;
        } else return r;
    }


    @Override
    public String getOrSetString(String patch, String def, boolean Colored) {
        if(Colored){
            return parseColor(getOrSetString(patch, def));
        } else return getOrSetString(patch, def);
    }


    @Override
    public int getOrSetInt(String patch, int def) {
        if(super.isInt(patch)){
            return super.getInt(patch);
        }
        else {
            super.set(patch, def);
            return def;
        }
    }


    @Override
    public boolean getOrSetBoolean(String patch, boolean def) {
        if(super.contains(patch)){
            if(super.isBoolean(patch)){
                return super.getBoolean(patch);
            }
        }
        super.set(patch, def);
        return def;
    }


    @Override
    public List<String> getOrSetStringList(String patch, List<String> def){
        if(isList(patch)){
            return super.getStringList(patch);
        }
        else {
            super.set(patch, def);
            return def;
        }
    }


    @Override
    public double getOrSetDouble(String patch, double def) {
        if(super.isDouble(patch)){
            return super.getDouble(patch);
        } else {
            super.set(patch, def);
            return def;
        }
    }


    @Override
    public Location getLocation(String path){
        if(isLocation(path)){
            Location loc = super.getLocation(path);
            set(path, codeLoc(loc));
            return loc;
        } else {
            return encodeLoc(getOrSetString(path, "world;0;0;0"));
        }
    }


    public void set(String path, Object o){
        super.set(path, o);
    }


    public void set(String path, Location loc){
        super.set(path, codeLoc(loc));
    }


    public static Location encodeLoc(String data){
        String[] d = data.split(";");
        if(d.length==3){
            double x = Double.parseDouble(d[0]);
            double y = Double.parseDouble(d[1]);
            double z = Double.parseDouble(d[2]);
            return new Location(Bukkit.getWorlds().get(0), x, y, z);
        } else {
            World w = Bukkit.getWorld(d[0]);
            if (w == null) {
                System.out.println(Bukkit.getWorlds().get(0));
                w = Bukkit.getWorlds().get(0);
            }
            double x = Double.parseDouble(d[1]);
            double y = Double.parseDouble(d[2]);
            double z = Double.parseDouble(d[3]);
            float yaw; float pith;
            if(d.length==4){ yaw = 0; pith = 0;
            } else {
                yaw = Float.parseFloat(d[4]);
                pith = Float.parseFloat(d[5]); }
            return new Location(w, x, y, z, yaw, pith);
        }
    }


    public static String codeLoc(Location loc){
        return loc.getWorld().getName()+";"+loc.getX()+";"+loc.getY()+";"+loc.getZ()+";"+loc.getYaw()+";"+loc.getPitch();
    }
    
    
    public static String parseColor(String string){
        return string.replace('&', '\u00A7');
    }
}