package plus.te4net.core.bsys;

import plus.te4net.utl.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;


public class NJarFileDataProvider extends HashMap<String, NJarFileDataProvider.JarItem> implements IDataProvider{
    public void include(String name, String jarPath, ClassLoader src){
        if(name.charAt(0)!='/'){
            name = '/'+name;
        }
        put(name, new JarItem(src, jarPath));
    }


    @Override
    public byte[] getRaw(String name, String table) {
        String path = NFileDataProvider.getPAth("", table, name);
        JarItem val = get(path);
        if(val==null)return null;
        return val.get();
    }


    @Override
    public void saveRaw(String name, String table, byte[] data) {
        Logger.get().info("Attempt to save ["+table+"]:["+name+"] to JAR! Ignored.");
    }


    @Override
    public boolean containsRaw(String name, String table) {
        String path = NFileDataProvider.getPAth("", table, name);
        return containsKey(path);
    }


    protected static final class JarItem {
        private final ClassLoader loader;
        private final String jarPath;
        private JarItem(ClassLoader loader, String jarPath) {
            this.loader = loader;
            this.jarPath = jarPath;
        }

        private byte[] get(){
            try {
                return read(jarPath);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        private byte[] read(String name) throws IOException {
            InputStream stream = loader.getResourceAsStream(name);
            if(stream==null)return null;
            byte[] result = stream.readAllBytes();
            stream.close();
            return result;
        }
    }
}
