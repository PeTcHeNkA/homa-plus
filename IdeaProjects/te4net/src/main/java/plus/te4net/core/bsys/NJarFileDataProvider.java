package plus.te4net.core.bsys;

import plus.te4net.core.node.HWorkerRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;


public class NJarFileDataProvider extends HashMap<String, NJarFileDataProvider.JarItem> implements IDataProvider{
    public void include(String name, String jarPath, ClassLoader src){
        if(name.charAt(0)=='/'){
            name = name.substring(1);
        }
        if(jarPath.charAt(0)=='/'){
            jarPath = jarPath.substring(1);
        }
        put(name, new JarItem(src, jarPath));
    }


    private String getPath(String[] path){
        if(path.length==2)return path[1];
        StringBuilder path01 = new StringBuilder(path[1]);
        for(int i=2;i<path.length;i++){
            path01.append('/').append(path[i]);
        }
        return path01.toString();
    }


    @Override
    public byte[] getRaw(String... path) {
        JarItem val = get(getPath(path));
        if(val==null)return null;
        return val.get();
    }


    @Override
    public void saveRaw(byte[] data, String... path) {
        String path0 = getPath(path);
        HWorkerRepository.getLogger().info("Attempt to save ["+path0+"] to JAR! Ignored.");
    }


    @Override
    public boolean containsRaw(String... path) {
        return containsKey(getPath(path));
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


    @Override
    public String[] getItems(String... path) {
        return keySet().toArray(new String[0]);
    }
}
