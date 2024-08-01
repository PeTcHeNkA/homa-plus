package plus.fserver.plugin;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import plus.tson.utl.Te4HashMap;
import plus.tson.utl.Te4HashSet;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;


public class TweakEngine implements IClassTransformer, Set<String> {
    private static final String URL_TWEAK_SEARCH_POINT = "plus/fserver/plugin/TweakEngine.class";
    private final ClassLoader loader = TweakEngine.class.getClassLoader();
    private final Te4HashMap<String, TweakInfo> classes = new Te4HashMap<>();
    private final Te4HashSet<String> preLoads = new Te4HashSet<>();
    private final String header;
    private final boolean debug = false;
    private final IClassTransformer sideTransformer;

    private static class TweakInfo{
        private boolean useSideTransformer;
        private boolean useSafeMode;

        public TweakInfo(boolean useSafeMode, boolean useSideTransformer){
            this.useSafeMode = useSafeMode;
            this.useSideTransformer = useSideTransformer;
        }
    }

    public TweakEngine(){
        this(URL_TWEAK_SEARCH_POINT);
    }


    public TweakEngine(String point) {
        if(FMLLaunchHandler.side()==null){
            sideTransformer = null;
        } else {
            IClassTransformer transformer = null;
            try {
                Class<IClassTransformer> clazz = (Class<IClassTransformer>) Class.forName(
                        "net.minecraftforge.fml.common.asm.transformers.SideTransformer"
                );
                transformer = clazz.newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            sideTransformer = transformer;
        }
        Enumeration<URL> urls = urls(point);
        header = urls.nextElement().getFile().split("!")[0];
        if (urls.hasMoreElements()) {
            System.out.println("ELCA POINT: "+header);
            while (urls.hasMoreElements()){
                System.out.println("UNSOLVABLE: "+urls.nextElement());
            }
        }
    }


    protected void preLoadClasses(){
        for (String name:preLoads)preLoad(name);
    }


   protected void preLoad(String name){
       try {
           Class.forName(name);
       } catch (ClassNotFoundException e) {
           throw new RuntimeException(e);
       }
   }


    @Override
    public int size() {
        return classes.size();
    }


    @Override
    public boolean isEmpty() {
        return classes.isEmpty();
    }


    @Override
    public boolean contains(Object o) {
        return classes.containsKey(o);
    }


    @NotNull
    @Override
    public Iterator<String> iterator() {
        return classes.keySet().iterator();
    }


    @NotNull
    @Override
    public Object[] toArray() {
        return classes.keySet().toArray();
    }


    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return classes.keySet().toArray(a);
    }


    @Override
    public boolean add(String name){
        return classes.put(name, new TweakInfo(false, true)) == null;
    }


    @Override
    public boolean remove(Object o) {
        return classes.remove(o) != null;
    }


    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return classes.keySet().containsAll(c);
    }


    @Override
    public boolean addAll(@NotNull Collection<? extends String> c) {
        for (String s:c){
            add(s);
        }
        return true;
    }


    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        for (Object s:c) remove(s);
        return true;
    }


    @Override
    public void clear() {
        classes.clear();
    }


    public void add(String name, boolean safe){
        classes.fput(name, new TweakInfo(safe, true));
    }


    public void add(String name, boolean safe, boolean preLoad){
        add(name, safe);
        if(preLoad) preLoads.add(name);
    }


    public void add(String name, boolean safe, boolean preLoad, boolean useTransformers){
        classes.fput(name, new TweakInfo(safe, useTransformers));
        if(preLoad) preLoads.add(name);
    }


    private String prepareName(String name) {
        return name.replace(".", "/") + ".class";
    }


    public byte[] tweakBytesIfNeed(String name) {
        TweakInfo info = classes.get(name);
        if (info == null) return null;
        byte[] result = info.useSafeMode ? getSafeTweaks(name) : getFastTweak(name);
        if(sideTransformer == null || !info.useSideTransformer)return result;
        return sideTransformer.transform(name, name, result);
    }


    private byte[] getFastTweak(String name) {
        if(debug){
            System.out.println("ATTEMPT TWEAK "+name);
        }
        return getFastSrc(prepareName(name));
    }


    protected byte[] getFastSrc(String name){
        InputStream stream = loader.getResourceAsStream(name);
        if (stream == null)return notFound(name);
        byte[] result = null;
        try {
            result = IOUtils.toByteArray(stream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException ignored) {}
        }
        return result;
    }


    private byte[] notFound(String name) {
        if(debug) {
            System.err.println("NULL CLASS DATA: " + name);
        }
        return null;
    }


    private byte[] getSafeTweaks(String name) {
        if(debug){
            System.out.println("ATTEMPT SAFE TWEAK "+name);
        }
        return getSafeSrc(prepareName(name));
    }


    protected final byte[] getSafeSrc(String name){
        Enumeration<URL> urls = urls(name);
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