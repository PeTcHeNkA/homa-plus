package plus.nlaunch;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import static plus.nlaunch.NLaunch.*;


public class NULaunch extends URLClassLoader {
    public NULaunch(ClassLoader parent) {
        super(get(), parent);
        NLaunch.inst = this;
        String packs = System.getProperty("nlaunch.lars");
        if(packs!=null){
            main(packs.split(";"));
        }
    }
    public NULaunch(){
        this(getSystemClassLoader());
    }

    private static java.net.URL[] get(){
        String cp = System.getProperty("nlaunch.cp");
        if(cp==null)return new URL[]{};
        String[] files = cp.split(";");
        URL[] urls = new URL[files.length];
        for(int i=0;i< urls.length;i++){
            try {
                urls[i] = new File(files[i]).toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return urls;
    }


    @Override
    public InputStream getResourceAsStream(String name) {
        l1: if(canGetSafe) {
            byte[] res = getSafe(name);
            if (res == null || res.length == 0) break l1;
            return new ByteArrayInputStream(res);
        }
        return super.getResourceAsStream(name);
    }


    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if(canGetSafe) {
            Class<?> clz = loadSafeClass(name);
            if (clz != null) {
                return clz;
            }
        }
        return super.findClass(name);
    }
}