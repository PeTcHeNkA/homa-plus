package plus.forge;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;


public class TweakEngine implements IClassTransformer {
    private static final String URL_TWEAK_SEARCH_POINT = "plus/fserver/plugin/TweakEngine.class";
    private final ClassLoader loader = TweakEngine.class.getClassLoader();
    private final HashMap<String, Boolean> classes = new HashMap<>();
    private final String header;
    private final boolean debug = false;

    public TweakEngine(){
        this(URL_TWEAK_SEARCH_POINT);
    }


    public TweakEngine(String point) {
        Enumeration<URL> urls = urls(point);
        header = urls.nextElement().getFile().split("!")[0];
        if (urls.hasMoreElements()) throw new RuntimeException();
    }


    public void add(String name){
        classes.put(name, false);
    }


    public void add(String name, boolean safe){
        classes.put(name, safe);
    }


    public static String prepareName(String name) {
        return name.replace(".", "/") + ".class";
    }


    public byte[] tweakBytesIfNeed(String name) {
        Boolean mode = classes.get(name);
        if (mode == null) return null;
        return mode ? getSafeTweaks(name) : getFastTweak(name);
    }


    private byte[] getFastTweak(String name) {
        if(debug){
            System.out.println("ATTEMPT TWEAK "+name);
        }
        InputStream stream = loader.getResourceAsStream(prepareName(name));
        if (stream == null) {
            return notFound(name);
        }
        try {
            byte[] result = IOUtils.toByteArray(stream);
            stream.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static byte[] notFound(String name) {
        System.err.println("NULL CLASS DATA: " + name);
        return null;
    }


    private byte[] getSafeTweaks(String name) {
        if(debug){
            System.out.println("ATTEMPT SAFE TWEAK "+name);
        }
        Enumeration<URL> urls = urls(prepareName(name));
        URL usedUrl = null;
        while (urls.hasMoreElements()) {
            URL cur = urls.nextElement();
            String curHeader = cur.getFile().split("!")[0];
            if (header.equals(curHeader)) {
                usedUrl = cur;
            }
        }
        if(debug){
            System.out.println("USD URL: "+usedUrl);
        }
        if (usedUrl == null) {
            return notFound(name);
        }
        try {
            InputStream stream = (InputStream) usedUrl.getContent();
            byte[] result = IOUtils.toByteArray(stream);
            stream.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return notFound(name);
        }
    }


    private Enumeration<URL> urls(String patch) {
        try {
            return loader.getResources(patch);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        byte[] result = tweakBytesIfNeed(transformedName);
        if(result != null)return result;
        return basicClass;
    }
}