package plus.te4net.core.node;

import io.netty.handler.ssl.*;
import plus.asyutl.shoulder.TaskShoulder;
import plus.asyutl.thread.PopThreadedPool;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.bsys.DataProviderBuilder;
import plus.te4net.core.bsys.IDataProvider;
import plus.te4net.core.cons.ConsoleCommand;
import plus.te4net.core.cons.ConsoleListener;
import plus.te4net.core.html.CssFile;
import plus.te4net.core.html.JsFile;
import plus.te4net.core.html.StaticHtmlFile;
import plus.te4net.utl.Logger;
import plus.te4net.utl.NCHashMap;
import plus.tson.*;
import plus.tson.format.IVarGetter;
import plus.tson.security.ClassManager;
import plus.tson.utl.Tuple;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;


public final class HWorkerRepository implements ClassManager {
    private final HServer server;
    final HashMap<String, IVarGetter<IRequestSession>> functions = new HashMap<>();
    public static final PopThreadedPool<Runnable> pool = new PopThreadedPool<>(4);
    public static final TaskShoulder shoulder = new TaskShoulder(500);
    private final NCHashMap<HWorkerContext> workers = new NCHashMap<>();
    private final IDataProvider defDataPovider;
    private final TsonFile config;
    final int maxPostMemory;


    HWorkerRepository(TsonMap map, HServer server){
        this.server = server;
        defDataPovider = DataProviderBuilder.ofTson(
                null, map.getOrDefault("cache",new TsonStr("sql:root:cache.cache"))
        );
        config = new TsonFile(this,
                map.getOrDefault("config", new TsonStr("config.tson")).getStr()
        );
        for(String domain:config.getMap("domains").keySet()){
            loadWorker(domain);
        }
        maxPostMemory = config.getOrDefault("max-post-mem",new TsonInt(8*1024*1024*24)).getInt();
        if(map.getOrDefault("init-console", new TsonBool(true)).getBool()){
            initConsole();
        }
    }


    private void initConsole(){
        new ConsoleListener().addCommand(
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
                                    switch (args[1]){
                                        case "on":
                                            server.startHttp(HWorkerRepository.this, config);
                                            onComplete(args);
                                            return;
                                        case "off":
                                            server.stopHttp();
                                            onComplete(args);
                                            return;
                                    }
                                    break;
                                case "https":
                                    switch (args[1]){
                                        case "on":
                                            server.startHttps(HWorkerRepository.this, config);
                                            onComplete(args);
                                            return;
                                        case "off":
                                            server.stopHttps();
                                            onComplete(args);
                                            return;
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
        ).start();
    }


    void unloadWorker(String key){
        if(workers.containsKey(key)){
            HWorkerContext ctx = workers.get(key);
            for (String domain:ctx.domains){
                workers.remove(domain);
            }
            Logger.get().info(ctx+" removed.");
        } else {
            Logger.get().info("Worker ["+key+"] not defined!");
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


    HWorkerContext getFor(String domain){
        return workers.get(domain);
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


    public IDataProvider getDefDataPovider() {
        return defDataPovider;
    }


    public static class HWorkerContext {
        public final HWorker worker;
        final ArrayList<String> domains;
        private final SslContext cert;

        private HWorkerContext(HWorker worker, ArrayList<String> domains, File workDir, String domain){
            this.worker = worker;
            this.domains = domains;
            this.cert = readSSL(workDir, domain);
        }


        private static ArrayList<Tuple<String,HWorkerContext>> build(HWorkerRepository repository, String domain, TsonMap config){
            File wdir = getWorkDir(config, domain);
            HWorkerContext main = new HWorkerContext(repository,wdir,domain,config);
            ArrayList<Tuple<String,HWorkerContext>> lst = new ArrayList<>();
            lst.add(new Tuple<>(domain, main));
            if(config.containsKey("domains")){
                for(TsonObj obj:config.getList("domains")){
                    String newDomain = obj.getStr();
                    main.domains.add(newDomain);
                    HWorkerContext alt = new HWorkerContext(main.worker, main.domains, wdir, newDomain);
                    lst.add(new Tuple<>(newDomain, alt));
                }
            }
            return lst;
        }


        private HWorkerContext(HWorkerRepository repository, File wdir, String domain, TsonMap config) {
            domains = new ArrayList<>();
            domains.add(domain);
            this.cert = readSSL(wdir, domain);
            boolean alowHttp = (cert == null) || (
                    config.getOrDefault("allow-http",new TsonBool(false)).getBool()
            );
            this.worker = new HWorker(
                    this, repository, wdir, domain, config, alowHttp, cert!=null
            );
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


        private SslContext readSSL(File workDir, String domain){
            try {
                return SslContextBuilder.forServer(
                        new FileInputStream(new File(workDir, domain+".ser")),
                        new FileInputStream(new File(workDir, domain+".key"))
//                ).sslProvider(
//                        SslProvider.OPENSSL
                ).applicationProtocolConfig(new ApplicationProtocolConfig(
                        ApplicationProtocolConfig.Protocol.ALPN,
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2,
                        ApplicationProtocolNames.HTTP_1_1
                )).build();
            } catch (Exception e){
                Logger.get().info("SSL not loaded! files [" +
                        workDir.getPath().replace('\\','/')+"/"+domain+".ser, "
                        + workDir.getPath().replace('\\','/')+"/"+domain+".key] not readable.");
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
}