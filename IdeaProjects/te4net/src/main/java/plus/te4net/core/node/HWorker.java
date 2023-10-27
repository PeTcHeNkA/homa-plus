package plus.te4net.core.node;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.ssl.SslContext;
import plus.te4net.Args;
import plus.te4net.core.api.IEventHandler;
import plus.te4net.core.api.IHttpClient;
import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.event.EventBus;
import plus.te4net.core.event.events.VrHtmlEvent;
import plus.te4net.core.html.*;
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
import plus.te4net.mds.auth.UserData;
import plus.te4net.mds.proc.AuthRoute;
import plus.tson.*;
import plus.tson.format.ConstVarGetter;
import plus.tson.format.IVarGetter;
import plus.tson.format.IVarProvider;
import plus.util.PCLTask;

import java.io.File;
import java.util.HashMap;


public final class HWorker implements IVarProvider<IRequestSession>, IAuthProvider, IDataProvider {
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
        initEventSystem();
        this.mainDomain = domain;
        this.allowHttp = allowHttp;
        this.hasCert = hasCert;

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
                new File(workDir, "static"), HWorkerRepository::buildStaticHtml)
        );
        if(!map.getOrDefault("use_cdn", new TsonBool(false)).getBool()) {
            addRoute("assets/img", new NCachedImgProcessor(imageProvider));
        } else {
            addRoute("assets/img", new NSimpleImgProcessor(imageProvider));
        }
        HPluginSystem loader = loadPlugins(workDir, map);
        provider = buildAuthProvider(loader, map);
        loadPages(workDir);
        loadRoutes(workDir, map.getMap("routes"));
    }


    void initEventSystem(){
        EventBus bus = new EventBus();
        repository.objBus.put(EventBus.class, this, bus);
        bus.addEventType(
                VrHtmlEvent.class
        );
    }


    public void addEventType(Class<?> classes){
        repository.objBus.get(EventBus.class, this).addEventType(classes);
    }


    public void throwEvent(Object obj){
        repository.objBus.get(EventBus.class, this).throwEvent(obj);
    }


    public <T> void addEventHandler(IEventHandler<T> handler){
        repository.objBus.get(EventBus.class, this).addEventHandler(handler);
    }


    public <T> void addEventHandler(Class<T> clazz, IEventHandler<T> handler){
        repository.objBus.get(EventBus.class, this).addEventHandler(clazz, handler);
    }


    public File getWorkDir(){
        return repository.getWorkDir(this);
    }


    private void loadRoutes(File workdir, TsonMap map){
        if(map==null)return;
        for(String key:map.keySet()){
            addRoute(key, repository.buildRoute(this, workdir, key, map.getMap(key)));
        }
    }


    private IAuthProvider buildAuthProvider(HPluginSystem loader, TsonMap map){
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
            HPluginSystem plSys = new HPluginSystem(this, map);
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
        //System.out.printf("register route [%s]: [%s]\n", path, processor);
        routes.put(path, processor);
    }


    public void handle(IRequestSession session){
        IRequestProcessor processor = routes.get(session);
        if(processor == null){
            session.notFound();
            //TODO DEBUG ONLY
//            if(repository.show404){
                System.out.println(session);
//            }
        } else {
            try {
                processor.handle(session);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public IHttpClient client(){
        return repository.client();
    }


    public IHttpClient client(SslContext ctx){
        return repository.client(ctx);
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
        HWorkerRepository.getLogger().info("Var ["+name+"] not exist!");
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
    public void update(User user) {
        provider.update(user);
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