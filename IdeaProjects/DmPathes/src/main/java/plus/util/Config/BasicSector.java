package plus.util.Config;

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
            //path = path + "backups" + File.separator + name.replace(".", "_"+n + ".");
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
    public String getColoredString(String path) {
        return super.getString(path, "").replace('&', '\u00A7');
    }


    @Override
    public String getColoredString(String path, String def) {
        return super.getString(path, def).replace('&', '\u00A7');
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
        String r = super.getString(patch);
        if(r==null){
            super.set(patch, def);
            if(Colored)
                return def.replace('&', '\u00A7');
            else return def;
        } else if(Colored) return def.replace('&', '\u00A7');
        else return r;
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
        if(!super.isList(patch) || !super.contains(patch) || super.getStringList(patch).size()==0){
            super.set(patch, def);
        }
        return def;
    }

}
