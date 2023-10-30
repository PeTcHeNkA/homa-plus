package plus.te4net.core.node;

import io.netty.handler.ssl.*;
import plus.asyutl.shoulder.IBasicTask;
import plus.asyutl.shoulder.TaskShoulder;
import plus.asyutl.thread.PopThreadedPool;
import plus.te4net.core.api.*;
import plus.te4net.core.bsys.DataProviderBuilder;
import plus.te4net.core.bsys.IDataProvider;
import plus.te4net.core.event.EventBus;
import plus.te4net.core.event.ObjectBus;
import plus.te4net.core.event.events.VrHtmlEvent;
import plus.te4net.core.html.*;
import plus.util.cons.ConsoleCommand;
import plus.util.cons.ConsoleListener;
import plus.util.cons.Logger;
import plus.te4net.utl.NCHashMap;
import plus.tson.*;
import plus.tson.format.IVarGetter;
import plus.tson.security.ClassManager;
import plus.tson.utl.Tuple;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import static plus.te4net.core.html.CGzipRoute.DEF_CACHE_TIME;


public final class HWorkerRepository implements ClassManager {
    private static Logger logger;
    private final HServer server;
    final HashMap<String, IVarGetter<IRequestSession>> functions = new HashMap<>();
    public static final PopThreadedPool<Runnable> pool = new PopThreadedPool<>(4);
    public static final TaskShoulder shoulder = new TaskShoulder(500);
    private final NCHashMap<HWorkerContext> workers = new NCHashMap<>();
    private final IDataProvider defDataPovider;
    private final TsonFile config;
    final int maxPostMemory;
    final ObjectBus objBus = new ObjectBus();

    public static Logger getLogger(){
        return logger;
    }


    HWorkerRepository(TsonMap map, HServer server){
        this.server = server;
        logger = Logger.get("tehnet");
        if(map.getOrDefault("init-console", new TsonBool(true)).getBool()){
            initConsole("tehnet");
        }
        defDataPovider = DataProviderBuilder.ofTson(
                null, map.getOrDefault("cache",new TsonStr("sql:root:cache.cache"))
        );
        config = new TsonFile(this,
                map.getOrDefault("config", new TsonStr("config.tson")).getStr()
        );
        maxPostMemory = config.getOrDefault("max-post-mem",new TsonInt(8*1024*1024*24)).getInt();

        //objBus.put(ObjectBus.class, this, new ObjectBus());
        for(String domain:config.getMap("domains").keySet()){
            loadWorker(domain);
        }
        //objBus.remove(ObjectBus.class, this);
    }

    private void initConsole(String name){
        new ConsoleListener(name).addCommand(
                new ConsoleCommand("/unload") {
                    @Override
                    public void onCommand(String... args) {
                        unloadWorker(args[0]);
                    }
                },
                new ConsoleCommand("/load") {
                    @Override
                    public void onCommand(String... args) {
                        loadWorker(args[0]);
                    }
                },
                new ConsoleCommand("/workers") {
                    @Override
                    public void onCommand(String... args) {
                        int i = 0;
                        for (HWorkerContext ctx : workers.values()) {
                            System.out.println(i++ + "|" + ctx);
                        }
                    }
                },
                new ConsoleCommand("/reload-all") {
                    @Override
                    public void onCommand(String... args) {
                        for(String s:HWorkerRepository.this.config.getMap("domains").keySet()){
                            unloadWorker(s);
                            loadWorker(s);
                        }
                    }

                    @Override
                    public String help() {
                        return "Restart all workers";
                    }
                },
                new ConsoleCommand("/node") {
                    @Override
                    public void onCommand(String... args) {
                        if(args != null && args.length>1){
                            switch (args[0]){
                                case "http":
                                    switch (args[1]) {
                                        case "on" -> {
                                            server.startHttp(HWorkerRepository.this, config);
                                            onComplete(args);
                                            return;
                                        }
                                        case "off" -> {
                                            server.stopHttp();
                                            onComplete(args);
                                            return;
                                        }
                                    }
                                    break;
                                case "https":
                                    switch (args[1]) {
                                        case "on" -> {
                                            server.startHttps(HWorkerRepository.this, config);
                                            onComplete(args);
                                            return;
                                        }
                                        case "off" -> {
                                            server.stopHttps();
                                            onComplete(args);
                                            return;
                                        }
                                    }
                                    break;
                            }
                        }
                        System.out.println(help());
                    }
                    void onComplete(String[] args){
                        System.out.println("Node ["+args[0]+"] is ["+args[1]+"]");
                    }
                    @Override
                    public String help() {
                        return "[http/https] [on/off]";
                    }
                }
        );
    }


    void unloadWorker(String key0){
        int realKey = key0.hashCode();
        if(workers.containsKey(realKey)){
            HWorkerContext ctx = workers.get(realKey);
            for (String domain:ctx.domains){
                workers.remove(domain.hashCode());
            }
            logger.info(ctx+" removed.");
        } else {
            logger.info("Worker ["+key0+"] not defined!");
        }
    }


    void loadWorker(String domain){
        TsonMap config = this.config.getMap("domains");
        boolean flag = false;
        if(!config.containsKey(domain)){
            TsonMap map = new TsonMap();
            config.put(domain, map);
            flag = true;
        }
        ArrayList<Tuple<String, HWorkerContext>> group = HWorkerContext.build(this, domain, config.getMap(domain));
        for(Tuple<String, HWorkerContext> item:group){
            workers.putSafe(item.A, item.B);
        }
        if(flag){
            this.config.save();
        }
    }


    public ObjectBus objectBus() {
        return objBus;
    }


    HWorkerContext getFor(String domain){
        return workers.get(domain);
    }


    IHttpClient client(){
        return server.client();
    }


    IHttpClient client(SslContext ctx){
        return server.client(ctx);
    }


    static JsFile buildJS(HWorker worker, File file){
        return new JsFile(worker, file);
    }


    static CssFile buildCSs(HWorker worker, File file){
        return new CssFile(worker, file);
    }


    static StaticHtmlFile buildStaticHtml(HWorker worker, File file){
        return new StaticHtmlFile(worker, file);
    }


    static boolean isTemp(Class<?> processor){
        return processor.isAssignableFrom(TempRequsetProcessor.class);
    }


    private static class TempContainer implements IRequestProcessor{
        private int markUse;
        private int curMark;
        private int notUsed = 0;
        private TempRequsetProcessor parent;
        private final HPCLTask task;
        private TempContainer(HPCLTask task) {
            this.task = task;
        }
        @Override
        public void handle(IRequestSession session) {
            if(parent==null){
                parent = (TempRequsetProcessor) task.inst();
            }
            parent.handle(session);
            curMark = markUse;
            notUsed = 0;
        }

        public void check(int realMark){
            if(markUse!=realMark){
                curMark = realMark;
                if(parent==null)return;
                if(notUsed++>6){
                    parent.onRemove(task.worker);
                    parent = null;
                    notUsed = 0;
                }
            }
        }
    }

    private TempContainer[] tempContainers;
    private int curMark = 0;

    void onAddTempRoute(HPCLTask task){
        if(tempContainers==null){
            tempContainers = new TempContainer[]{};
            shoulder.addTask(new IBasicTask() {
                private int timer = 0;
                @Override
                public boolean needRun() {
                    if(timer++>30){
                        timer = 0;
                        return true;
                    }
                    return false;
                }
                @Override
                public void run() {
                    for (TempContainer ctn:tempContainers){
                        ctn.check(curMark);
                    }
                    curMark++;
                }
            });
        }
        TempContainer ctn = new TempContainer(task);
        ctn.curMark = ctn.markUse = curMark;
        TempContainer[] ntasks = new TempContainer[tempContainers.length + 1];
        System.arraycopy(tempContainers, 0, ntasks, 0, tempContainers.length);
        tempContainers = ntasks;
        ntasks[tempContainers.length-1] = ctn;
    }


    public IDataProvider getDefDataPovider() {
        return defDataPovider;
    }


    File getWorkDir(HWorker worker){
        return getWorkDir(config.getMap(worker.getMainDomain()), worker.getMainDomain());
    }


    private static File getWorkDir(TsonMap map, String domain){
        File file;
        if(map.containsKey("wdir")){
            file = new File(map.getStr("wdir"));
        } else {
            file = new File("domains/"+domain);
        }
        if(!file.exists())file.mkdirs();
        return file;
    }


    public static class HWorkerContext {
        public final HWorker worker;
        final ArrayList<String> domains;
        private final SslContext cert;

        private HWorkerContext(HWorker worker, ArrayList<String> domains, File workDir, String domain, boolean useLetsEncrypt){
            this.worker = worker;
            this.domains = domains;
            this.cert = readSSL(workDir, domain, useLetsEncrypt);
        }


        private static ArrayList<Tuple<String,HWorkerContext>> build(HWorkerRepository repository, String domain, TsonMap config){
            File wdir = getWorkDir(config, domain);
            boolean useLetsEncrypt = config.getOrDefault("use-lets-encrypt",new TsonBool(false)).getBool();

            HWorkerContext main = new HWorkerContext(repository, wdir, domain, config, useLetsEncrypt);
            ArrayList<Tuple<String,HWorkerContext>> lst = new ArrayList<>();
            lst.add(new Tuple<>(domain, main));
            if(config.containsKey("domains")){
                for(TsonObj obj:config.getList("domains")){
                    String newDomain = obj.getStr();
                    main.domains.add(newDomain);
                    HWorkerContext alt = new HWorkerContext(main.worker, main.domains, wdir, newDomain, useLetsEncrypt);
                    lst.add(new Tuple<>(newDomain, alt));
                }
            }
            return lst;
        }


        private HWorkerContext(HWorkerRepository repository, File wdir, String domain, TsonMap config, boolean useLetsEncrypt) {
            domains = new ArrayList<>();
            domains.add(domain);

            this.cert = readSSL(wdir, domain, useLetsEncrypt);
            boolean alowHttp = (cert == null) || (
                    config.getOrDefault("allow-http",new TsonBool(false)).getBool()
            );
            this.worker = new HWorker(
                    this, repository, wdir, domain, config, alowHttp, cert!=null
            );
        }


        private SslContext readSSL(File workDir, String domain, boolean letsEncrypt){
            File publicKey = null;
            File privateKey = null;
            try {
                boolean flag = false;
                if(letsEncrypt){
                    publicKey = new File("/etc/letsencrypt/live/"+domain+"/fullchain.pem");
                    privateKey = new File("/etc/letsencrypt/live/"+domain+"/privkey.pem");
                    flag = privateKey.isFile() && publicKey.isFile();
                    if(!flag){
                        logger.info("LetsEncrypt certificate for ["+domain+"] not configured. Used STD.");
                    }
                }
                if(!flag){
                    publicKey = new File(workDir, domain+".ser");
                    privateKey = new File(workDir, domain+".key");
                }
                return SslContextBuilder.forServer(
                        new FileInputStream(publicKey),
                        new FileInputStream(privateKey)
                ).applicationProtocolConfig(new ApplicationProtocolConfig(
                        ApplicationProtocolConfig.Protocol.ALPN,
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2,
                        ApplicationProtocolNames.HTTP_1_1
                )).build();
            } catch (Exception e){
                logger.info("SSL not loaded! files [" +
                        publicKey.getPath().replace('\\','/')+", "+
                        privateKey.getPath().replace('\\','/')+" not readable.");
                return null;
            }
        }


        @Override
        public String toString() {
            if(domains.size()==1) {
                return domains.get(0);
            } else {
                return domains.get(1)+" and "+(domains.size()-1)+" etc";
            }
        }

        SslContext getCert() {
            return cert;
        }
    }


    static {
        preLoad(org.sqlite.JDBC.class);
        preLoad(com.luciad.imageio.webp.WebPImageReaderSpi.class);
    }
    private static void preLoad(Class<?>... classes){}


    IRequestProcessor buildRoute(HWorker worker, File workDir, String patch, TsonMap map){
        String type = map.getOrDefault("type", new TsonStr("none")).getStr();
        File root = new File(workDir, map.getOrDefault("dir", new TsonStr(patch)).getStr());
        switch (type){
            case "files":
            case "file-three":
                return new FileThreeProc(worker, root,
                        map.getOrDefault("cache",new TsonInt(DEF_CACHE_TIME)).getInt()
                );
        }
        return CGzipRoute.build(worker, root, map);
    }
}