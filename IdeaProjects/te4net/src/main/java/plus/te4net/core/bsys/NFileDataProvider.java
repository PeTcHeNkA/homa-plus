package plus.te4net.core.bsys;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;


public class NFileDataProvider implements IDataProvider{
    private final String dir;
    private final boolean trimTable;
    public NFileDataProvider(String dir) {
        this.dir = dir;
        trimTable = false;
    }
    public NFileDataProvider(String dir, boolean trimTable) {
        this.dir = dir;
        this.trimTable = trimTable;
    }


    static String getPAth(String path, String table, String name){
        if(table.length()>0) {
            if (table.charAt(0) == '/') {
                if (table.charAt(table.length() - 1) == '/') {
                    table = table.substring(1, table.length() - 1);
                } else {
                    table = table.substring(1);
                }
            } else {
                if (table.charAt(table.length() - 1) == '/') {
                    table = table.substring(1, table.length() - 1);
                }
            }
        }
        if(table.isBlank()) {
            return path + "/" + name;
        } else {
            return path+ "/" + table + "/"+name;
        }
    }


    @Override
    public byte[] getRaw(String name, String table) {
        if(trimTable){
            int index = table.indexOf("/");
            if(index==-1)table = "";
            else table = table.substring(index);
        }
        File file = new File(getPAth(dir, table, name));
        if(!file.isFile())return null;
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public void saveRaw(String name, String table, byte[] data) {
        File dir0 = new File(dir + "/" + table);
        if(!dir0.exists()){
            dir0.mkdirs();
        }
        try {
            Files.write(FileSystems.getDefault().getPath(dir + "/"+table+"/"+name),data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean containsRaw(String name, String table) {
        File file = new File(dir + "/" + table+"/"+name);
        return file.isFile();
    }
}
