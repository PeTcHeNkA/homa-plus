package plus.te4net.core.node;

import io.netty.handler.ssl.*;
import plus.asyutl.shoulder.IBasicTask;
import plus.te4net.core.api.*;
import plus.te4net.core.bsys.DataProviderBuilder;
import plus.te4net.core.api.IDataProvider;
import plus.te4net.mds.html.*;
import plus.tson.utl.Te4HashMap;
import plus.util.cons.ConsoleCommand;
import plus.util.cons.ConsoleListener;
import plus.tson.*;
import plus.tson.format.IVarGetter;
import plus.tson.security.ClassManager;
import plus.tson.utl.Tuple;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static plus.te4net.core.node.HServer.preLoad;
import static plus.te4net.mds.html.CGzipRoute.DEF_CACHE_TIME;


final class HEWorkerRepository extends HWorkerRepositoryBase implements ClassManager {
    final Te4HashMap<String, IVarGetter<RequestSession>> functions = new Te4HashMap<>();
    private TempContainer[] tempContainers;
    private final IDataProvider defDataPovider;
    private int curMark = 0;

    HEWorkerRepository(TsonMap map, HServer server){
        super(server, new TsonMap());
        if(map.getOrDefault("init-console", new TsonBool(true)).getBool()){
            initConsole("tehnet");
        }
        defDataPovider = DataProviderBuilder.ofTson(server.workerGroup,
                (IDataProvider) null, map.getOrDefault("cache",new TsonStr("sql:root:cache.cache"))
        );
        TsonFile.fillMap(config, this,
                new File(map.getOrDefault("config", new TsonStr("config.tson")).getStr()), ""
        );
        maxPostMemory = config.getOrDefault("max-post-mem",new TsonInt(maxPostMemory)).getInt();

        for(String domain:config.getMap("domains").keySet()){
            loadWorker(domain);
        }
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
                        for(String s: HEWorkerRepository.this.config.getMap("domains").keySet()){
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
                                            server.startHttp(HEWorkerRepository.this, config);
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
                                            server.startHttps(HEWorkerRepository.this, config);
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
            HServer.getLogger().info(ctx+" removed.");
        } else {
            HServer.getLogger().info("Worker ["+key0+"] not defined!");
        }
    }


    void loadWorker(String domain){
        TsonMap config = this.config.getMap("domains");
        if(!config.containsKey(domain)){
            TsonMap map = new TsonMap();
            config.put(domain, map);
        }
        ArrayList<Tuple<String, HWorkerContext>> group = HWorkerContext.build(this, domain, config.getMap(domain));
        for(Tuple<String, HWorkerContext> item:group){
            workers.putSafe(item.A, item.B);
        }
    }


    static JsFile buildJS(HEWorker worker, File file){
        return new JsFile(worker, file);
    }


    static CssFile buildCSs(HEWorker worker, File file){
        return new CssFile(worker, file);
    }


    static StaticHtmlFile buildStaticHtml(HEWorker worker, File file){
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
        public void handle(RequestSession session) {
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


    @Override
    protected void onAddTempRoute(HPCLTask task){
        if(tempContainers==null){
            tempContainers = new TempContainer[]{};
            task.worker.pool().scheduleWithFixedDelay(() -> {
                for (TempContainer ctn:tempContainers){
                    ctn.check(curMark);
                }
            }, 5_000,5_000, TimeUnit.MILLISECONDS);
        }
        TempContainer ctn = new TempContainer(task);
        ctn.curMark = ctn.markUse = curMark;
        TempContainer[] ntasks = new TempContainer[tempContainers.length + 1];
        System.arraycopy(tempContainers, 0, ntasks, 0, tempContainers.length);
        tempContainers = ntasks;
        ntasks[tempContainers.length-1] = ctn;
    }


    @Override
    File getWorkDir(HWorker worker){
        return getWorkDir(config.getMap(worker.getMainDomain()), worker.getMainDomain());
    }


    @Override
    protected IDataProvider getDefDataProvider() {
        return defDataPovider;
    }


    IRequestProcessor buildRoute(HEWorker worker, File workDir, String patch, TsonMap map){
        String type = map.getOrDefault("type", new TsonStr("none")).getStr();
        File root = new File(workDir, map.getOrDefault("dir", new TsonStr(patch)).getStr());
        TsonObj headers = map.get("headers");
        IRequestProcessor result;
        switch (type){
            case "files":
            case "file-three":
                result = new FileThreeProc(worker, root,
                        map.getOrDefault("cache",new TsonInt(DEF_CACHE_TIME)).getInt()
                );
                break;
            default:
                result = CGzipRoute.build(worker, root, map);
                break;
        }
        if(headers != null){
            result = new TupleProcessor(HeadersProcessor.build(headers), result);
        }
        return result;
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


    static class HWorkerContext {
        public final HWorker worker;
        private final SslContext cert;
        final ArrayList<String> domains;

        private HWorkerContext(HWorker worker, ArrayList<String> domains, File workDir, String domain, boolean useLetsEncrypt){
            this.worker = worker;
            this.domains = domains;
            this.cert = readSSL(workDir, domain, useLetsEncrypt);
        }


        HWorkerContext(HWorker worker){
            this.worker = worker;
            this.domains = null;
            cert = null;
        }


        HWorkerContext(HWorker worker, InputStream pubKey, InputStream privKey){
            this.worker = worker;
            this.domains = null;
            cert = readSslSafe(pubKey, privKey);
        }


        private static ArrayList<Tuple<String,HWorkerContext>> build(HEWorkerRepository repository, String domain, TsonMap config){
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


        private HWorkerContext(HEWorkerRepository repository, File wdir, String domain, TsonMap config, boolean useLetsEncrypt) {
            domains = new ArrayList<>();
            domains.add(domain);

            this.cert = readSSL(wdir, domain, useLetsEncrypt);
            boolean alowHttp = (cert == null) || (
                    config.getOrDefault("allow-http",new TsonBool(false)).getBool()
            );
            this.worker = new HEWorker(
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
                        HServer.getLogger().info("LetsEncrypt certificate for ["+domain+"] not configured. Used STD.");
                    }
                }
                if(!flag){
                    publicKey = new File(workDir, domain+".ser");
                    privateKey = new File(workDir, domain+".key");
                }
                return readSsl(new FileInputStream(publicKey), new FileInputStream(privateKey));
            } catch (Exception e){
                HServer.getLogger().info("SSL not loaded! files [" +
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
}