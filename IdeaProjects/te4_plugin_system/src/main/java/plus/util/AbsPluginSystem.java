package plus.util;

import plus.tson.*;
import plus.tson.security.ClassManager;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;


public abstract class AbsPluginSystem<T extends PluginClassLoader> implements ClassManager, IPluginProcessor<Object>{
    private HashMap<String, ClassLoader> allLoaders = new HashMap<>();
    private ArrayList<PCLTask> allPlugins = new ArrayList<>();
    private final boolean needLog;
    public AbsPluginSystem(){
        this(true);
    }

    public AbsPluginSystem(boolean onlyError){
        this.needLog = !onlyError;
        if(needLog) Logger.getGlobal().info("Start loading plugins...");
    }


    public void load(String... names){
        for(String n:names){
            File file = new File(n);
            if(file.exists())load(file);
        }
    }


    @Override
    public Class<?> forName(String clazz) {
        if(clazz.contains("#")){
            String[] s = clazz.split("#");
            ClassLoader loader = allLoaders.get(s[0]);
            if(loader != null) {
                try {
                    return loader.loadClass(s[1]);
                } catch (ClassNotFoundException e){
                    return null;
                }
            }
            return null;
        }
        for (ClassLoader loader:allLoaders.values()){
            try {
                return loader.loadClass(clazz);
            } catch (ClassNotFoundException ignored){}
        }
        return null;
    }


    public void load(File... files){
        Logger logger = Logger.getGlobal();
        if(allPlugins==null)throw new RuntimeException("All Plugins ready uploaded!");
        for(File file:files) {
            if (file.isDirectory()) {
                File[] files0 = file.listFiles();
                if (files0 == null) {
                    logger.info("Plugins in ["+file.getPath()+"] not exists!");
                    return;
                }
                boolean flag = true;
                for (File subFile : files0) {
                    if(!filter(subFile)){
                        logger.info("["+file.getPath()+"] not file/plugin!");
                        continue;
                    }
                    flag = false;
                    allPlugins.addAll(Arrays.asList(loadPluginsFromFile(subFile)));
                }
                if(flag){
                    logger.info("Plugins in ["+file.getPath()+"] not exists!");
                }
            } else {
                if(!filter(file)){
                    logger.info("["+file.getPath()+"] not file/plugin!");
                    return;
                }
                allPlugins.addAll(Arrays.asList(loadPluginsFromFile(file)));
            }
        }
    }


    protected boolean filter(File file){
        return file.isFile() && file.getName().endsWith("jar");
    }


    protected void addToReg(PCLTask task){
        if(allPlugins==null)throw new RuntimeException("All Plugins ready uploaded!");
        allPlugins.add(task);
    }


    public void completeLoad(){
        if(allPlugins==null)throw new RuntimeException("All Plugins ready uploaded!");
        ArrayList<Object> postInits = new ArrayList<>();
        IPluginProcessor[] processors = processors();
        Logger logger = Logger.getGlobal();

        allPlugins.sort(Comparator.comparingInt(o -> o.priority));

        for(IPluginProcessor proc:processors){
            for(PCLTask plugin:allPlugins) {
                if(plugin==null || plugin.isLoaded())continue;
                if(proc.getSupportedType().isAssignableFrom(plugin.clazz)){
                    try {
                        Object o = plugin.inst();
                        postInits.add(o);
                        proc.onLoad(o);
                        if(needLog) logger.info("Plugin ["+proc.getClass().getSimpleName()+"] -> ["
                                +plugin.getName()+"] loaded."
                        );
                        plugin.markLoaded();
                    } catch (Exception e){
                        logger.info("Error for enabling plugin ["+plugin.clazz.getName()+"]");
                        e.printStackTrace();
                    }
                }

            }
        }
        int ignored = 0;
        for(PCLTask plugin:allPlugins){
            if(!plugin.isLoaded()){
                logger.info("Plugin ["+plugin.getName()+"] ignored.");
                ++ignored;
            }
        }
        for(Object o:postInits){
            onPostInit(o);
        }
        for(IPluginProcessor proc:processors){
            proc.onComplete();
        }
        int activePlugins = postInits.size();
        if(needLog) logger.info("Plugins loaded: "+(activePlugins-ignored)+"/"+activePlugins);
        allPlugins = null;
    }


    protected void onPostInit(Object o){}


    protected abstract IPluginProcessor<?>[] processors();


    protected PCLTask[] loadPluginsFromFile(File file) {
        try {
            T loader = newClassloader(file);
            allLoaders.put(file.getPath(), loader);
            TsonMap data = getPluginInfo(loader);
            String[] names = new String[]{"Plugin"};

            //URL url = loader.getResource("META-INF/MANIFEST.MF");
            AppPCLTask appTask = null;
//            if(url != null) {
//                System.out.println(url);
//
//                byte[] mainSrc = new byte[0];
//                try {
//                    mainSrc = url.openStream().readAllBytes();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                //byte[] mainSrc = getSrc(loader, "META-INF/MANIFEST.MF");
//
//                if (mainSrc != null) {
//                    String s = new String(mainSrc, StandardCharsets.UTF_8);
//                    for (String line : s.split("\n")) {
//                        if (line.startsWith("Main-Class")) {
//                            appTask = new AppPCLTask(
//                                    Class.forName(line.split(":")[1].trim(), true, loader)
//                            );
//                            break;
//                        }
//                    }
//                }
//            }

            if (data.containsKey("class")) {
                TsonObj obj = data.get("class");
                if(obj.isString()){
                    names = new String[]{obj.getStr()};
                } else if (obj.isList()) {
                    TsonList list = obj.getList();
                    names = new String[list.size()];
                    for(int i = 0;i<list.size();i++){
                        names[i] = list.getStr(i);
                    }
                }
            }
            PCLTask[] pclTasks = new PCLTask[appTask==null?names.length:names.length+1];
            for(int i=0;i<names.length;i++){
                pclTasks[i] = newPCLTask(Class.forName(names[i], true, loader));
            }
            if(appTask!=null){
                pclTasks[names.length] = appTask;
            }
            return pclTasks;
        } catch (Throwable e) {
            System.err.println("CRITICAL ERROR ON LOADING PLUGIN ["
                    +file.toString().replace("\\","/")+"]"
            );
            e.printStackTrace();
            return new PCLTask[]{};
        }
    }


    protected PCLTask newPCLTask(Class<?> clazz){
        return new PCLTask(clazz);
    }


    protected T newClassloader(File... files) throws MalformedURLException {
        return (T) new PluginClassLoader(files);
    }


    private TsonMap getMapSrc(ClassLoader loader, String name){
        byte[] src = getSrc(loader, name);
        if(src==null)return null;
        return new TsonMap(this, new String(src, StandardCharsets.UTF_8));
    }


    protected void onLoadConfig(T loader, TsonMap map) {

    }


    protected void secOnLoadConfig(T loader, TsonMap map) {
        if(map==null)return;
        if(map.containsKey("include")){
            TsonObj obj = map.get("include");
            if(obj.isList()){
                for(TsonObj obj0:obj.getList()){
                    secOnLoadConfig(loader, getMapSrc(loader, obj0.getStr()));
                }
            } else {
                secOnLoadConfig(loader, getMapSrc(loader, obj.getStr()));
            }
        }
        onLoadConfig(loader, map);
    }


    private TsonMap getPluginInfo(T loader){
        byte[] data = getSrc(loader, "plugin.tson");
        TsonMap result;
        if(data==null) result = new TsonMap();
        else result = new TsonMap(this, new String(data)
                .replace("\r","").replace("\t","    ")
        );
        secOnLoadConfig(loader, result);
        return result;
    }


    protected static byte[] getSrc(ClassLoader loader, String name) {
        try {
            InputStream stream = loader.getResourceAsStream(name);
            if(stream==null)return null;
            byte[] result = stream.readAllBytes();
            stream.close();
            return result;
        } catch (Exception e) {
            return null;
        }
    }


    @Override
    public void onLoad(Object obj) {}


    @Override
    public Class<?> getSupportedType() {
        return Object.class;
    }
}