package plus.quest;

import org.bukkit.plugin.Plugin;
import plus.util.cfg.Config;
import java.util.ArrayList;
import java.util.List;


public class QuestConfig extends Config {

    public QuestConfig(Plugin p, String name) {
        super(p, name);
    }


    @Override
    public String getOrSetString(String path, String def){
        return getOrSetRaw(path, def, new defFunc() {
            @Override public <T> T getOrSet(String path, T def) {
                return (T) QuestConfig.super.getOrSetString(path, (String) def) ;
            }
        });
    }


    @Override
    public int getOrSetInt(String path, int def){
        return getOrSetRaw(path, def, new defFunc() {
            @Override public <T> T getOrSet(String path, T def) {
                Integer i = QuestConfig.super.getOrSetInt(path, (Integer) def);
                return (T) i;
            }
            @Override public <T> T cast(String path){
                Integer i = Integer.parseInt(path);
                return (T) i;
            }
        });
    }


    public String getScriptType(String path, String def){
        String lastKey = path.contains(".")?path.substring(path.lastIndexOf(".") + 1):path;
        if(lastKey.contains("=")){
            return lastKey.substring(lastKey.lastIndexOf("=") + 1).strip();
        } else {
            return getOrSetString(path+".type", def);
        }
    }


    public List<String> getOrSetSingleStringList(String path, String defKey, List<String> defData){
        if(isString(path)){
            return parseStringToArray(getString(path));
        } else {
            return getOrSetStringList(path+defKey, defData);
        }
    }


    public List<String> getOrSetStringList(String path, List<String> defData){
        if(isString(path)){
            return parseStringToArray(getString(path));
        } else {
            return super.getOrSetStringList(path, defData);
        }
    }


    private List<String> parseStringToArray(String s){
        ArrayList<String> data = new ArrayList<>();
        if(s.charAt(0)=='[' && s.charAt(s.length()-1)==']'){
            s = s.substring(1, s.length()-1);
        }
        if(!s.contains("'")){
            data.add(s);
            return data;
        }

        StringBuilder bufer = new StringBuilder();
        boolean searchN = false;

        for(char c:s.toCharArray()){
            if(c=='\''){
                searchN = !searchN;
            } else {
                if(!searchN){
                    if(c==',' && !bufer.isEmpty()){
                        data.add(bufer.toString());
                        bufer = new StringBuilder();
                        searchN = false;
                    }
                } else {
                    bufer.append(c);
                }
            }
        }
        if(!bufer.isEmpty()) data.add(bufer.toString());
        return data;
    }


    public int getSingleInt(String path, String subKey, int def){
        if(isInt(path)) return getInt(path);
        else return getOrSetInt(path+subKey, def);
    }


    public String getSingleString(String path, String subKey, String def){
        if(isString(path)) {
            String s = getString(path);
            String request = subKey+"=";
            if(s.startsWith(request))return s.substring(request.length());
            else return s;
        }
        else return getOrSetString(path+subKey, def);
    }


    @Override
    public boolean getOrSetBoolean(String path, boolean def){
        //System.out.println(String.format("|> getOrSetBool %s <=> %s", path, def));
        return getOrSetRaw(path, def, new defFunc() {
            @Override public <T> T getOrSet(String path, T def) {
                Boolean b = QuestConfig.super.getOrSetBoolean(path, (Boolean) def);
                return (T) b;
            }
            @Override public <T> T cast(String path){
                Boolean b = path.equalsIgnoreCase("true");
                return (T) b;
            }
        });
    }


    public <T> T getOrSetRaw(String path, T def, defFunc func){
        if(path.contains(".")) {
            String parentPath = path.substring(0, path.lastIndexOf("."));
            if(isString(parentPath)){
                String parentKey = path.substring(path.lastIndexOf(".")+1);

                String[] data = getString(parentPath).strip().split(",");

                for(String s:data){
                    if(s.isEmpty())continue;
                    int subIndex = s.indexOf("=");
                    if(subIndex==-1)continue;
                    String key = s.substring(0, subIndex).strip();

                    if(key.equals(parentKey)) {
                        String value = s.substring(subIndex + 1).strip();

                        if (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'') {
                            value = value.substring(1, value.length() - 1);
                        }
                        //System.out.println(String.format("|> "+path+ " -> "+value));
                        return func.cast(value);
                    }
                }

                String newData = getString(parentPath)+","+parentKey+"="+"'"+def+"'";
                set(parentPath, newData);
                saveChanges();
                return def;

            } else return func.getOrSet(path, def);
        } else return func.getOrSet(path, def);
    }
}


interface defFunc{
    <T> T getOrSet(String path, T def);

    default <T> T cast(String path){
        return (T) path;
    }
}