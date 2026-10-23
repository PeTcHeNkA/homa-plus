package plus.nlaunch;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;


public final class NLaunch extends ClassLoader{
    static HashMap<String,Class<?>> loadedClasses = new HashMap<>();
    static ClassLoader inst;
    static boolean canGetSafe = false;
    static native byte[]   getSafe (String var);
    static native Class<?> loadSafe(String var);

    static{
        try {
            String patch = System.getProperty("nlaunch.lib","nlaunch.dll");
            if(patch!=null){
                Runtime.getRuntime().load(patch);
                canGetSafe = true;
            } else {
                System.err.println("CANT LOAD NLAUNCH NATIVE: NULL");
            }
        } catch (Throwable e){
            System.err.println("CANT LOAD NLAUNCH NATIVE: CRITICAL ERROR");
            e.getStackTrace();
        }
    }


    public static void useAsLoader(ClassLoader loader){
        inst = loader;
    }


    public static void main(String... args) {
        //System.out.println("EVAL: "+ String.join(" ",args));
        for(String arg:args) {
            if (arg.charAt(0) == '*') {
                try {
                    loadSafe(arg.substring(1));
                    Class<?> cl = Class.forName(arg.substring(1));
                } catch (ClassNotFoundException err) {
                    throw new RuntimeException(err);
                }
            } else {
                byte[] res = getSafe(arg);
                if(res != null && res.length>0) System.out.println(new String(res));
            }
        }
    }


    public NLaunch(ClassLoader parent){
        super(parent);
        inst = this;
    }


    public NLaunch(){
        this(getSystemClassLoader());
    }


    @Override
    public InputStream getResourceAsStream(String name) {
        l1: if(canGetSafe) {
            byte[] res = getSafe(name);
            if (res == null || res.length == 0)break l1;
            return new ByteArrayInputStream(res);
        }
        return super.getResourceAsStream(name);
    }


    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if(canGetSafe) {
            Class<?> clz = loadSafeClass(name);
            if (clz != null) return clz;
        }
        return super.findClass(name);
    }


    public static Class<?> loadSafeClass(String var){
        if(!canGetSafe)return null;
        Class<?> res = loadedClasses.get(var);
        if (res == null) {
            loadedClasses.put(var, res = loadSafe(var));
        }
        return res;
    }
}