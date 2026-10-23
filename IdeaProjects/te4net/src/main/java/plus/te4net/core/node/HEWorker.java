package plus.te4net.core.node;

import io.netty.handler.codec.http.HttpResponseStatus;
import plus.te4net.core.Args;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.api.PublicUsage;
import plus.te4net.core.event.ObjectBus;
import plus.te4net.mds.html.CGzipRoute;
import plus.te4net.mds.html.HtmlRoute;
import plus.te4net.mds.html.SfHtmlRoute;
import plus.te4net.mds.img.CompressorSettings;
import plus.te4net.mds.img.NCachedImgProcessor;
import plus.te4net.mds.img.NSimpleImgProcessor;
import plus.te4net.core.api.IAuthProvider;
import plus.te4net.core.bsys.DataProviderBuilder;
import plus.te4net.core.api.IDataProvider;
import plus.te4net.core.mapping.QueryGetter;
import plus.te4net.core.mapping.SplatGetter;
import plus.te4net.mds.proc.AuthRoute;
import plus.te4net.mds.proc.IconRoute;
import plus.tson.*;
import plus.tson.format.ConstVarGetter;
import plus.tson.format.IVarGetter;
import plus.tson.format.IVarProvider;
import plus.tson.utl.Te4HashMap;
import java.io.File;


@PublicUsage
public final class HEWorker extends HWorker implements IVarProvider<RequestSession>, IDataProvider {
    private final Te4HashMap<String, IVarGetter<RequestSession>> functions = new Te4HashMap<>();
    final CompressorSettings settings = new CompressorSettings();
    private final HEWorkerRepository.HWorkerContext ctx;
    private final CGzipRoute cssLib;
    private final CGzipRoute jsLib;
    private final IDataProvider imageProvider;
    private final IDataProvider cacheProvider;
    private final IAuthProvider provider;

    HEWorker(HEWorkerRepository.HWorkerContext ctx, HEWorkerRepository repository, File workDir, String domain, TsonMap map, boolean allowHttp, boolean hasCert) {
        super(repository, allowHttp, hasCert, domain);
        this.ctx = ctx;
        initEventSystem();

        cacheProvider = DataProviderBuilder.ofTson(this,
                map.getOrDefault("cache", new TsonStr("root"))
        );

        cssLib = CGzipRoute.build(this, new File(workDir, "assets/css"),
                map.getOrDefault("assets-css",new TsonMap("{type='css'}")).getMap()
        );
        jsLib = CGzipRoute.build(this, new File(workDir, "assets/js"),
                map.getOrDefault("assets-js",new TsonMap("{type='js'}")).getMap()
        );

        File svgFile = new File(workDir, "assets/svg");
        if(svgFile.exists()) {
            CGzipRoute svg = CGzipRoute.build(this, svgFile,
                    map.getOrDefault("assets-svg", new TsonMap("{type='svg'}")).getMap()
            );
            addRoute("assets/svg", svg);
        }

        imageProvider = DataProviderBuilder.ofTson(this,
                map.getOrDefault("images", new TsonMap(
        "{root='root',"+domain+"/ORIG=['root:lochost.ru/','dir:domains/"+domain+"/assets/img:true']}"
                ))
        );
        addRoute("assets/css", cssLib);
        addRoute("assets/js",jsLib);
        addRoute("static",new CGzipRoute(this,
                new File(workDir, "static"), HEWorkerRepository::buildStaticHtml)
        );
        if(!map.getOrDefault("use_cdn", new TsonBool(false)).getBool()) {
            addRoute("assets/img", new NCachedImgProcessor(pool(), imageProvider));
        } else {
            addRoute("assets/img", new NSimpleImgProcessor(imageProvider));
        }
        HEPluginSystem loader = null;
        try {
            loader = loadPlugins(workDir, map);
        } catch (Throwable e){
            e.printStackTrace();
        }
        provider = buildAuthProvider(loader, map);
        loadPages(workDir);
        loadRoutes(workDir, map.getMap("routes"));
        loadIcon(workDir, map);
    }


    private void loadIcon(File workdir, TsonMap map){
        String path = map.getOrDefaultStr("icon", "any");
        File file = null;
        if(path.equals("any")){//"favicon.ico"
            for (String name:new String[]{"favicon.png","icon.png",
                    "ico.png","favicon.jpg","icon.jpg","ico.jpg"}){
                file = new File(workdir, name);
                if(file.exists())break;
                else file = null;
            }
        } else {
            file = new File(workdir, path);
            if(!file.exists())file = null;
        }
        if(file != null){
            addRoute("favicon.ico", new IconRoute(this, file));
        }
    }


    private void loadRoutes(File workdir, TsonMap map){
        if(map==null)return;
        HEWorkerRepository repo = ((HEWorkerRepository) repository);
        for(String key:map.keySet()){
            addRoute(key, repo.buildRoute(this, workdir, key, map.getMap(key)));
        }
    }


    private IAuthProvider buildAuthProvider(HEPluginSystem loader, TsonMap map){
        if(loader != null && loader.authProvider != null){
            return loader.authProvider;
        }
        TsonObj oauth = map.get("auth");
        if(oauth==null)return null;

        if(oauth.isCustom()) return (IAuthProvider) oauth.getField();

        if(oauth.isMap()){
            map = oauth.getMap();
            oauth = map.get("class");
            if(oauth==null){
                oauth = new TsonStr("default");
            }
        }

        if(oauth.isString()){
            if(oauth.getStr().equals("default")){
                return new AuthRoute<>(this, map);
            }
            return (IAuthProvider) new HPCLTask(
                    new TsonClass(loader, oauth.getStr()).getField(),
                    this,
                    map
            ).inst();
        }

        throw new IllegalArgumentException(String.format("Unsupported constructor for [%s]: <%s>", oauth, map));
    }


    private void loadPages(File workDir){
        File pgsFile = new File(workDir, "pages");
        if(!pgsFile.exists()){
            pgsFile.mkdirs();
            return;
        }
        for(String file: pgsFile.list()){
            if(file.endsWith(".html")){
                loadPage(file,"",pgsFile);
            }
        }
        for(String file:workDir.list()){
            if(file.endsWith(".html")){
                loadPage(file,"",workDir);
            }
        }
    }


    private void loadPage(String name, String path, File pgsFile){
        String path0 = pgsFile.getPath() + "/" + path + name;
        boolean useAuth = name.charAt(0)=='!';
        if(useAuth){
            name = name.substring(1);
        }
        String registredName = path+name.substring(0,name.length()-5);
        HtmlRoute hrt;
        if(useAuth){
            hrt = new SfHtmlRoute(this, path0, registredName);
        } else {
            hrt = new HtmlRoute(this, path0, registredName);
        }
        if(registredName.equals("index")){
            registredName = "";
        }
        addRoute(registredName, hrt);
    }


    private HEPluginSystem loadPlugins(File workDir, TsonMap map){
        File pluginDir = new File(workDir,"plugins");
        TsonObj plugins = map.get("plugins");
        if(!pluginDir.exists()){
            pluginDir.mkdirs();
            if(plugins==null)return null;
        }
        if(plugins==null && pluginDir.isDirectory() && pluginDir.list().length==0)return null;
        try {
            HEPluginSystem plSys = new HEPluginSystem(this, map);
            plSys.load(pluginDir);
            if(plugins != null) {
                if (plugins.isString()) {
                    String strPlugins = plugins.getStr();
                    if(strPlugins.equals("plugins"))return plSys;
                    plSys.load(new File(workDir, strPlugins));
                } else if (plugins.isList()){
                    for(TsonObj obj:plugins.getList()){
                        String strPlugins = obj.getStr();
                        if(strPlugins.equals("plugins"))return plSys;
                        plSys.load(new File(workDir, strPlugins));
                    }
                }
            }
            plSys.completeLoad();
            return plSys;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public String[] getItems(String... path) {
        return cacheProvider.getItems(path);
    }


    @Override
    public byte[] getRaw(String... path) {
        return cacheProvider.getRaw(expand(path));
    }


    @Override
    public void saveRaw(byte[] data, String... path) {
        cacheProvider.saveRaw(data, expand(path));
    }


    private String[] expand(String[] prev){
        String[] result = new String[prev.length+1];
        System.arraycopy(prev, 0, result, 1, prev.length);
        result[0] = mainDomain;
        return result;
    }


    public IDataProvider getDefDataPovider() {
        return repository.getDefDataProvider();
    }


    public ObjectBus objectBus() {
        return repository.objBus;
    }


    void onAddTempRoute(HPCLTask task){
        repository.onAddTempRoute(task);
    }


    public void regFunction(String name, IVarGetter<RequestSession> function, boolean root){
        if(root) {
            ((HEWorkerRepository)repository).functions.fput(name, function);
        } else {
            functions.fput(name, function);
        }
    }


    @Override
    public IVarGetter<RequestSession> getFmGetter(String name){
        if(ctx.domains.size()==1) {
            String domain = ctx.domains.get(0);
            String host = "https://"+domain;
            if (name.equals("host")) {
                if(allowHttp){
                    return session -> session.isSafe()?"https://"+domain:"http://"+domain;
                } else return new ConstVarGetter<>(host);
            }
            if (name.equals("domain")) return new ConstVarGetter<>(domain);
        } else {
            if (name.equals("domain")){
                return RequestSession::getDomain;
            }
            if(name.equals("host")){
                return session -> session.isSafe() ? "https://" + session.getDomain() :
                        "http://" + session.getDomain();
            }
        }
        if(name.startsWith("q:")){
            return new QueryGetter(name.substring(2));
        }
        if(name.startsWith("a:")){
            return new ConstVarGetter<>(Args.ARGS.getStr(name.substring(2)));
        }
        if(name.startsWith("splat:")){
            return new SplatGetter(Integer.parseInt(name.substring(6)));
        }
        if(name.startsWith("func:")){
            return getFunction(name.substring(5));
        }
        if(name.startsWith("redirect:")){
            return new IVarGetter<>() {
                final String to = name.substring(9);
                @Override
                public String get(RequestSession request) {
                    request.send(to, HttpResponseStatus.PERMANENT_REDIRECT);
                    throw new RuntimeException();
                }
            };
        }
        getLogger().info("Var ["+name+"] not exist!");
        return new ConstVarGetter<>(name);
    }


    private IVarGetter<RequestSession> getFunction(String name){
        IVarGetter<RequestSession> func = functions.get(name);
        if(func==null)return ((HEWorkerRepository)repository).functions.get(name);
        return func;
    }


    public CGzipRoute getJsLib() {
        return jsLib;
    }


    public CGzipRoute getCssLib() {
        return cssLib;
    }


    public IDataProvider getImgLib(){
        return imageProvider;
    }


    public CompressorSettings getCompressorSettings() {
        return settings;
    }


    @Override
    public String toString() {
        return ctx+": r="+routes.size()+", f="+functions.size();
    }


    @Override
    public IAuthProvider getAuthSystem() {
        return provider;
    }


    public boolean multiHost(){
        return ctx.domains.size()>1;
    }
}