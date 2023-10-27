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


    @Override
    public byte[] getRaw(String... path) {
        File file = new File(dir, getPath(path));
        if(!file.isFile())return null;
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private String getPath(String[] path){
        int start = trimTable?1:0;
        StringBuilder path01 = new StringBuilder(path[start]);
        for(int i=1+start;i<path.length;i++){
            path01.append('/').append(path[i]);
        }
        return path01.toString();
    }


    @Override
    public void saveRaw(byte[] data, String... path) {
        String path0 = getPath(path);
        File file = new File(dir, path0).getParentFile();
        if(file != null && !file.exists()){
            file.mkdirs();
        }
        try {
            Files.write(FileSystems.getDefault().getPath(dir + "/"+path0),data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean containsRaw(String... path) {
        int start = trimTable?1:0;
        StringBuilder path01 = new StringBuilder(dir).append('/').append(path[start]);
        for(int i=start;i<path01.length();i++){
            path01.append('/').append(path[i]);
        }
        return new File(path01.toString()).isFile();
    }


    @Override
    public String[] getItems(String... path) {
        String path0 = getPath(path);
        File file = new File(dir, path0).getParentFile();
        if(file==null)return new String[0];
        String[] items = file.list();
        if(items==null)return new String[0];
        return items;
    }
}
