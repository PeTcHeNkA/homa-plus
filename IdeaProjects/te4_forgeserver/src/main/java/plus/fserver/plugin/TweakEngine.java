package plus.fserver.plugin;

import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import plus.tson.utl.Te4HashSet;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;


public class TweakEngine implements IClassTransformer, Set<String> {
    private final ClassLoader loader = TweakEngine.class.getClassLoader();
    private final Object2ByteOpenHashMap<String> classes = new Object2ByteOpenHashMap<>();
    {classes.defaultReturnValue(Byte.MIN_VALUE);}
    private Te4HashSet<String> preLoads = new Te4HashSet<>(8,0.8f);
    private final String header;
    private final boolean debug = false;
    private final IClassTransformer sideTransformer;
    private final IClassTransformer modAccesTransformer = tryFindModAccTransformer();


    private static boolean isUseSide(byte flags){
        return (flags & 1) == 1;
    }


    private static boolean isUseSafe(byte flags){
        return (flags & 3) == 3;
    }


    private static byte tweakInfo(boolean useSafeMode, boolean useSideTransformer){
        byte flags = 0;
        if(useSafeMode)flags |= 1;
        if(useSafeMode)flags |= 3;
        return flags;
    }


    private static IClassTransformer tryFindModAccTransformer(){
        try {
            //net.minecraftforge.fml.common.asm.transformers.ModAccessTransformer
            //todo access to embedded
            return new TweakAccessTransformer();
        } catch (Throwable e){
            e.printStackTrace();
            return null;
        }
    }


    public TweakEngine(String point) {
        if(FMLLaunchHandler.side()==null){
            sideTransformer = null;
        } else {
            IClassTransformer transformer = null;
            try {
                if(PathTransformer21.canUse()){
                    transformer = new PathTransformer21();
                } else {
                    transformer = new PathTransformer();
                }
            } catch (Throwable e) {
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
        Te4HashSet<String> pr;
        if((pr = preLoads) != null) {
            for (String name : pr) preLoad(name);
            preLoads = null;
        }
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
        return classes.put(name, tweakInfo(false, true)) == -1;
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
        classes.put(name, tweakInfo(safe, true));
    }


    public void add(String name, boolean safe, boolean preLoad){
        add(name, safe);
        if(preLoad) {
            Te4HashSet<String> pr;
            if((pr = preLoads) != null) pr.add(name);
        }
    }


    public void add(String name, boolean safe, boolean preLoad, boolean useTransformers){
        classes.put(name, tweakInfo(safe, useTransformers));
        if(preLoad) {
            Te4HashSet<String> pr;
            if((pr = preLoads) != null) pr.add(name);
        }
    }


    private String prepareName(String name) {
        return name.replace(".", "/") + ".class";
    }


    public byte[] transformAccess(String s1, String s2, byte[] src){
        IClassTransformer mat;
        if((mat = modAccesTransformer) == null)return src;
        return mat.transform(s1, s2, src);
    }


    public byte[] tweakBytesIfNeed(String raw, String name, byte[] src) {
        byte info = classes.getByte(name);
        if (info == Byte.MIN_VALUE) {
            return PathTransformer.transformFast(name, src);
        }
        byte[] result = isUseSafe(info) ? getSafeTweaks(name) : getFastTweak(name);
        if(result == null){
            if(isUseSide(info))
                return PathTransformer.transformFast(name, src);
            return src;
        } else {
            if(sideTransformer == null || !isUseSide(info))
                src = PathTransformer.transformFast(name, result);
            else
                src = sideTransformer.transform(name, name, result);
            return transformAccess(raw, name, src);
        }
    }


    private byte[] getFastTweak(String name) {
        if(debug){
            System.out.println("ATTEMPT TWEAK "+name);
        }
        return getSafeSrc(prepareName(name));
        //return getFastSrc(prepareName(name));
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


    private String curHeader;
    private boolean shouldInit = true;

    protected final byte[] getSafeSrc(String name){
        String curHeader = this.curHeader;
        if(curHeader == null && shouldInit){
            URL first = null;
            URL usedUrl = null;
            Enumeration<URL> urls = urls(name);
            while (urls.hasMoreElements()) {
                URL cur = urls.nextElement();
                String curHeader0 = cur.getFile().split("!")[0];
                if (header.equals(curHeader0)) {
                    curHeader0 = cur.toString().split("!")[0];
                    this.curHeader = curHeader = curHeader0;
                    break;
                } else {
                    if(first == null) first = cur;
                }
            }
            if(debug){
                if(usedUrl == null) usedUrl = first;
                System.out.println("USD URL: "+usedUrl);
            }
        }
        if (curHeader == null) {
            return getFastSrc(name);
        }

        try {
            URL usedUrl = new URL(curHeader+"!/"+name);
            InputStream stream = (InputStream) usedUrl.getContent();
            byte[] result = IOUtils.toByteArray(stream);
            stream.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return getFastSrc(name);
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
//        if(transformedName.equals("net.minecraft.server.MinecraftServer")){
//            System.out.println("NEN!");
//        }
        return tweakBytesIfNeed(name, transformedName, basicClass);
    }
}