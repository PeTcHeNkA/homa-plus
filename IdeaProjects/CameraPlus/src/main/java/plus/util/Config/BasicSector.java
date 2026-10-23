package plus.util.Config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class BasicSector extends YamlConfiguration implements SimpleSector {

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
    public String getOrSetIfExistString(String patch, String def) {
        String r = super.getString(patch);
        if(r==null){
            super.set(patch, def);
            return def;
        } else return r;
    }

    @Override
    public int getOrSetIfExistInt(String patch, int def) {
        if(super.isInt(patch)){
            return super.getInt(patch);
        }
        else {
            super.set(patch, def);
            return def;
        }
    }

    @Override
    public boolean getOrSetIfExistBoolean(String patch, boolean def) {
        if(super.contains(patch)){
            if(super.isBoolean(patch)){
                return super.getBoolean(patch);
            }
        }
        super.set(patch, def);
        return def;
    }

    @Override
    public List<String> getOrCreateStringList(String patch, List<String> def){
        if(!super.isList(patch) || !super.contains(patch) || super.getStringList(patch).size()==0){
            super.set(patch, def);
        }
        return def;
    }

}
