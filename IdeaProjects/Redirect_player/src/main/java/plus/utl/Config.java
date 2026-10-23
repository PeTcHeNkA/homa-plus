package plus.utl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public class Config{
    protected final File file;
    protected String content = "";
    protected final HashMap<String, String> values = new HashMap<>();
    protected final HashMap<String, Boolean> booleans = new HashMap<>();

    public Config(File file){
        this.file = file;
        reload();
    }
    public Config(String filePatch){
        this.file = new File(filePatch);
        reload();
    }

    public String getString(String key){
        return values.get(key);
    }

    public boolean getBoolean(String key){
        if(booleans.containsKey(key)){
            return booleans.get(key);
        } else if(values.containsKey(key)) {
            String rv = values.remove(key);
            boolean b = rv.equalsIgnoreCase("true");
            booleans.put(key, b);
            return b;
        } else return false;
    }

    public void reload() {
        values.clear();
        booleans.clear();
        if(!file.exists()){
            try {
                file.createNewFile();
                System.out.println(String.format("File [%s] not founded! all variables is null!", file.toPath()));
            } catch (IOException ignored) {}
        } else {
            try {
                ParseContent(new String(Files.readAllBytes(file.toPath())));
            }catch (Exception e){
                System.out.println("Syntax error!");
                e.printStackTrace();
            }
        }
    }

    public void Write(String key, Object value){
        try(FileWriter writer = new FileWriter(file, false)) {
            if(values.containsKey(key)) {
                content = content.replace("\n"+key, "\n#"+key);
            }
            content = content+String.format("\n%s: %s", key, value);
            values.put(key, value.toString());
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected void ParseContent(String content){
        this.content = content;
        String[] strings = content.split("\n");
        for(String s:strings){
            if(s.length()<1)continue;
            if(s.contains("#")){
                s = s.substring(0, s.indexOf('#'));
            }

            int index = s.indexOf(':');
            if(index>0){
                values.put(
                        s.substring(0, index).strip(),
                        s.substring(index+1).strip()
                );
            }
        }
    }


    public String getOrWrite(String key, String def, String comment){
        String result = values.get(key);
        if(result == null){
            result = def==null?"null":def;
            Write(key, result);
            addComment(comment);
        }
        if(result.equalsIgnoreCase("null"))return null;
        return result;
    }


    public String getOrWrite(String key, String def){
        String result = values.get(key);
        if(result == null){
            result = def==null?"null":def;
            Write(key, result);
        }
        if(result.equalsIgnoreCase("null"))return null;
        return result;
    }
    public void addComment(String value){
        try(FileWriter writer = new FileWriter(file, false)) {
            content = content+"\n#"+value;
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getRawContent(){
        return content;
    }
}