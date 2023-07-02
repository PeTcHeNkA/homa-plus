package plus.te4net.core.node;

import io.netty.handler.codec.http.HttpResponseStatus;
import plus.te4net.Args;
import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.img.CompressorSettings;
import plus.te4net.core.img.NCachedImgProcessor;
import plus.te4net.core.img.NSimpleImgProcessor;
import plus.te4net.core.mapping.HRouteMap;
import plus.te4net.mds.auth.IAuthProvider;
import plus.te4net.mds.auth.User;
import plus.te4net.core.bsys.DataProviderBuilder;
import plus.te4net.core.bsys.IDataProvider;
import plus.te4net.core.fm.QueryGetter;
import plus.te4net.core.fm.SplatGetter;
import plus.te4net.core.html.CGzipRoute;
import plus.te4net.core.html.HtmlRoute;
import plus.te4net.mds.auth.UserData;
import plus.te4net.mds.proc.AuthRoute;
import plus.te4net.core.html.SfHtmlRoute;
import plus.te4net.utl.Logger;
import plus.tson.TsonBool;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.TsonStr;
import plus.tson.format.ConstVarGetter;
import plus.tson.format.IVarGetter;
import plus.tson.format.IVarProvider;
import java.io.File;
import java.util.HashMap;


public final class HWorker implements IVarProvider<IRequestSession>, IAuthProvider {
    private final HashMap<String, IVarGetter<IRequestSession>> functions = new HashMap<>();
    final CompressorSettings settings = new CompressorSettings();
    private final HRouteMap routes = new HRouteMap();
    private final HWorkerRepository repository;
    private final HWorkerRepository.HWorkerContext ctx;
    private final String mainDomain;
    private final CGzipRoute cssLib;
    private final CGzipRoute jsLib;
    private final IDataProvider imageProvider;
    private final IDataProvider cacheProvider;
    private final IAuthProvider provider;
    private final boolean allowHttp;
    private final boolean hasCert;


    HWorker(HWorkerRepository.HWorkerContext ctx, HWorkerRepository repository, File workDir, String domain, TsonMap map, boolean allowHttp, boolean hasCert) {
        this.ctx = ctx;
        this.repository = repository;
        this.mainDomain = domain;
        this.allowHttp = allowHttp;
        this.hasCert = hasCert;
        cssLib = new CGzipRoute(this, new File(workDir, "assets/css"), HWorkerRepository::buildCSs);
        jsLib = new CGzipRoute(this, new File(workDir, "assets/js"), HWorkerRepository::buildJS);
        imageProvider = DataProviderBuilder.ofTson(this,
                map.getOrDefault("images", new TsonMap(
        "{root='root',"+domain+"/ORIG=['root:lochost.ru/','dir:domains/"+domain+"/assets/img:true']}"
                ))
        );
        cacheProvider = DataProviderBuilder.ofTson(this,
                map.getOrDefault("cache", new TsonStr("root"))
        );
        addRoute("assets/css", cssLib);
        addRoute("assets/js",jsLib);
        addRoute("static",new CGzipRoute(this,
                new File(workDir, "static"), HWorkerRepository::buildStaticHtml)
        );
        if(!map.getOrDefault("use_cdn", new TsonBool(false)).getBool()) {
            addRoute("assets/img", new NCachedImgProcessor(imageProvider));
        } else {
            addRoute("assets/img", new NSimpleImgProcessor(imageProvider));
        }
        HPluginSystem loader = loadPlugins(workDir, map);
        provider = buildAuthProvider(workDir, loader, map);
        loadPages(workDir);
    }


    private IAuthProvider buildAuthProvider(File workdir, HPluginSystem loader, TsonMap map){
        if(loader != null && loader.authProvider != null){
            return loader.authProvider;
        }
        if(map.getOrDefault("use-std-auth",new TsonBool(false)).getBool()) {
            AuthRoute<User> ar = new AuthRoute<>(this, new UserData<>(workdir.getPath() + "/users.db") {
                @Override
                protected User newUser(String login, String password, byte[] data) {
                    return new User(login, password, data);
                }
            }, map.getOrDefault("mail",new TsonMap("{virtual=(true)}")).getMap());
            addRoute("auth", ar);
            return ar;
        } else {
            return null;
        }
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
        addRoute(registredName, hrt);
    }


    private HPluginSystem loadPlugins(File workDir, TsonMap map){
        File pluginDir = new File(workDir,"plugins");
        TsonObj plugins = map.get("plugins");
        if(!pluginDir.exists()){
            pluginDir.mkdirs();
            if(plugins==null)return null;
        }
        if(plugins==null && pluginDir.isDirectory() && pluginDir.list().length==0)return null;
        try {
            HPluginSystem plSys = new HPluginSystem(this);
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


    public void addRoute(String path, IRequestProcessor processor){
        routes.put(path, processor);
    }


    public void handle(IRequestSession session){
        IRequestProcessor processor = routes.get(session);
        if(processor == null){
            session.notFound();
        } else {
            try {
                processor.handle(session);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public byte[] getFromCache(String... path){
        return cacheProvider.getRaw(expand(path));
    }


    public void saveToCache(byte[] data, String... path){
        cacheProvider.saveRaw(data, expand(path));
    }


    private String[] expand(String[] prev){
        String[] result = new String[prev.length+1];
        System.arraycopy(prev, 0, result, 1, prev.length);
        result[0] = mainDomain;
        return result;
    }


    public boolean maybeSafe(){
        return hasCert;
    }


    public HWorkerRepository getRepository() {
        return repository;
    }


    public void regFunction(String name, IVarGetter<IRequestSession> function, boolean root){
        if(root) {
            repository.functions.put(name, function);
        } else {
            functions.put(name, function);
        }
    }


    @Override
    public IVarGetter<IRequestSession> getFmGetter(String name){
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
                return IRequestSession::getDomain;
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
                public String get(IRequestSession request) {
                    request.send(to, HttpResponseStatus.PERMANENT_REDIRECT);
                    throw new RuntimeException();
                }
            };
        }
        Logger.get().info("Var ["+name+"] not exist!");
        return new ConstVarGetter<>(name);
    }


    private IVarGetter<IRequestSession> getFunction(String name){
        IVarGetter<IRequestSession> func = functions.get(name);
        if(func==null)return repository.functions.get(name);
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
    public User auth(IRequestSession session) {
        return provider.auth(session);
    }


    @Override
    public void goToAuth(IRequestSession session) {
        provider.goToAuth(session);
    }


    @Override
    public String toString() {
        return ctx+": r="+routes.size()+", f="+functions.size();
    }


    public String getMainDomain() {
        return mainDomain;
    }


    public String getMainHost(){
        if(hasCert)return "https://"+getMainDomain();
        return "http://"+getMainDomain();
    }


    public boolean isAllowHttp() {
        return allowHttp;
    }


    public boolean multiHost(){
        return ctx.domains.size()>1;
    }
}