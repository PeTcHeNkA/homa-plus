package plus.te4net.core.node;

import plus.te4net.core.api.PluginRoute;
import plus.te4net.core.bsys.NHybrideDataProvider;
import plus.te4net.core.bsys.NJarFileDataProvider;
import plus.te4net.core.img.CompressorSettings;
import plus.te4net.core.bsys.IDataProvider;
import plus.te4net.mds.auth.IAuthProvider;
import plus.te4net.mds.plsys.FunctionProcessor;
import plus.te4net.mds.plsys.RouteProcessor;
import plus.te4net.core.html.CGzipRoute;
import plus.te4net.core.html.HtmlRoute;
import plus.te4net.core.html.SfHtmlRoute;
import plus.tson.*;
import plus.util.AbsPluginSystem;
import plus.util.IPluginProcessor;
import plus.util.PCLTask;
import plus.util.PluginClassLoader;
import static plus.te4net.core.img.CompressorSettings.ofMap;


public class HPluginSystem extends AbsPluginSystem<PluginClassLoader> {
    private final HWorker worker;
    private final TsonMap config;
    protected IAuthProvider authProvider;

    HPluginSystem(HWorker worker, TsonMap config) {
        super(true);
        this.worker = worker;
        this.config = config;
    }


    @Override
    protected IPluginProcessor<?>[] processors() {
        return new IPluginProcessor[]{
                new FunctionProcessor(worker),
                new RouteProcessor(worker),
                this
        };
    }


    @Override
    protected PCLTask newPCLTask(Class<?> clazz) {
        HPCLTask task = new HPCLTask(clazz, worker, config);
        if(HWorkerRepository.isTemp(task.getClass())){
            worker.getRepository().onAddTempRoute(task);
            return null;
        }
        return task;
    }


    private void staticAssets(ClassLoader loader, TsonMap map){
        includeAll(map, worker.getJsLib(),"js");
        includeAll(map, worker.getCssLib(),"css");
        includeImages(loader, map);
    }


    private void includeAll(TsonMap map, CGzipRoute lib, String key){
        if(map.containsKey(key)){
            for(TsonObj obj: map.getList(key)){
                TsonList lst = obj.getList();
                lib.include(worker, lst.getStr(0), lst.getStr(1),
                        lst.size() > 2 && lst.getBool(2)
                );
            }
        }
    }


    @Override
    protected void onLoadConfig(PluginClassLoader loader, TsonMap map) {
        if(map.containsKey("htmls")){
            TsonObj data = map.get("htmls");
            if(data.isList()){
                initHtmls(loader, data.getList());
            } else {
                initHtml(loader, data.getStr());
            }
        }
        if(map.containsKey("assets")){
            staticAssets(loader, map.getMap("assets"));
        }
        if(map.containsKey("imgs-stn")){
            TsonObj data = map.get("imgs-stn");
            if(data.isMap()){
                includeCompressSettings(data.getMap());
            } else {
                HWorkerRepository.getLogger().info("[imgs-stn] ignored, not TsonMap!");
            }
        }
        super.onLoadConfig(loader, map);
    }


    private void includeCompressSettings(TsonMap map){
        for(String key: map.keySet()){
            try {
                CompressorSettings.ImageInfo settings = ofMap(map.get(key).getMap());
                if(key.contains(";")){
                    for(String subKey:key.split(";")){
                        worker.settings.addSettings(subKey, settings);
                    }
                } else {
                    worker.settings.addSettings(key, settings);
                }
            } catch (Exception e){
                HWorkerRepository.getLogger().info("Error in read ["+key+"] in [imgs-stn], ignored.");
                e.printStackTrace();
            }
        }
    }


    private void includeImages(ClassLoader loader, TsonMap map){
        NHybrideDataProvider hb;
        if(worker.getImgLib() instanceof NHybrideDataProvider){
            hb = (NHybrideDataProvider) worker.getImgLib();
        } else {
            throw new RuntimeException("Worker ["+worker+"] has unsupported image provider. use Hybrid.");
        }
        IDataProvider origProvider = hb.get(worker.getMainDomain() + "/ORIG/inc");
        NJarFileDataProvider provider;
        if(origProvider == null){
            provider = new NJarFileDataProvider();
            hb.include(worker.getMainDomain() + "/ORIG/inc", provider);
        } else {
            if(origProvider instanceof NJarFileDataProvider) {
                provider = (NJarFileDataProvider) origProvider;
            } else {
                throw new RuntimeException("Worker ["+worker+"] has unsupported image provider. Key ["
                                +worker.getMainDomain()+"/ORIG/inc] not NULL or JAR provider!");
            }
        }
        TsonObj data = map.get("img");
        if(data.isList()) {
            for (TsonObj o : data.getList()) {
                includeImg(provider, loader, o);
            }
        } else if (data.isString()) {
            includeImg(provider, loader, data);
        }
    }


    private void includeImg(NJarFileDataProvider provider, ClassLoader loader, TsonObj img){
        if(img.isString()) {
            String path = img.getStr();
            provider.include(path, path, loader);
        } else if (img.isList()) {
            TsonList lst = img.getList();
            provider.include(lst.getStr(0), lst.getStr(1), loader);
        } else if (img.isMap()) {
            TsonMap map = img.getMap();
            String name = map.getStr("name");
            provider.include(name, map.getOrDefault("path", new TsonStr(name)).getStr(), loader);
        }
    }


    private void initHtmls(PluginClassLoader loader, TsonList list){
        for(TsonObj o:list){
            initHtml(loader, o.getStr());
        }
    }


    private void initHtml(PluginClassLoader loader, String name){
        boolean flag = name.startsWith("!");
        if(flag){
            name = name.substring(1);
        }
        if(name.contains("#")){
            String[] data = name.split("#");
            addToReg(new PCLHtml(loader, data[0], data[1], flag));
        } else {
            addToReg(new PCLHtml(loader, name, name, flag));
        }
    }


    private final class PCLHtml extends PCLTask {
        private final String path, name;
        private final PluginClassLoader loader;
        private final boolean needAuth;
        public PCLHtml(PluginClassLoader loader, String path, String name, boolean needAuth) {
            super(PluginRoute.class);
            this.path = path;
            this.name = name;
            this.loader = loader;
            this.needAuth = needAuth;
        }
        @Override
        public Object inst() {
            byte[] data = getSrc(loader, path);
            if(data==null)throw new NullPointerException("Src not founded in ["+path+"]");
            return needAuth?
                    new SfHtmlRoute(worker, data, name):
                    new HtmlRoute(worker, data, name);
        }

        @Override
        protected String getName() {
            return path+" -> "+name;
        }
    }
}