package plus.util;

import plus.tson.TsonMap;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


public interface SimpleSrcGetter {
    static byte[] getSrc(Class c, String name){
        return AbsPluginSystem.getSrc(c.getClassLoader(), name);
    }

    default String getStrSrc(String name){
        return new String(getSrc(name), StandardCharsets.UTF_8).replace("\r","").replace("\t","    ");
    }


    default TsonMap getTson(String name){
        byte[] src = getSrc(name);
        if(src==null)throw new NullPointerException("Src by ["+name+"] is null!");
        return new TsonMap(new String(src).replace("\r","").replace("\t","    "));
    }


    default byte[] getSrc(String name){
        return getSrc(name, false);
    }


    default byte[] getSrc(String name, boolean onlyInn){
        Class clazz = this.getClass();
        if(!onlyInn) {
            File file = new File(PluginClassLoader.getSrcPath(clazz) + name);
            if (file.isFile()) {
                try {
                    return Files.readAllBytes(file.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            } else if (file.isDirectory()) {
                byte[] data = getSrc(clazz, name);
                if (data == null) {
                    String[] files = file.list();
                    if (files == null) {
                        return null;
                    }
                    data = String.join("\n", files).getBytes();
                } else {
                    String[] files = file.list();
                    if (files == null) {
                        return data;
                    }
                    byte[] data2 = (String.join("\n", files)).getBytes();
                    byte[] result = new byte[data.length + data2.length];
                    System.arraycopy(data, 0, result, 0, data.length);
                    System.arraycopy(data2, 0, result, data.length, data2.length);
                    return result;
                }
                return data;
            }
        }
        return getSrc(clazz, name);
    }
}
