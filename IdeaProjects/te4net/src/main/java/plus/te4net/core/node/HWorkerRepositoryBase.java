package plus.te4net.core.node;

import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import plus.te4net.core.api.HttpClient;
import plus.te4net.core.api.IDataProvider;
import plus.te4net.core.event.ObjectBus;
import plus.te4net.core.mapping.NCHashMap;
import plus.tson.TsonMap;
import javax.net.ssl.SSLException;
import java.io.File;
import java.io.InputStream;


abstract class HWorkerRepositoryBase {
    final ObjectBus objBus = new ObjectBus();
    final NCHashMap<HEWorkerRepository.HWorkerContext> workers = new NCHashMap<>();
    final HServer server;
    final TsonMap config;
    int maxPostMemory = 8*1024*1024*24;

    HWorkerRepositoryBase(HServer server, TsonMap config) {
        this.server = server;
        this.config = config;
    }


    protected HEWorkerRepository.HWorkerContext getFor(String domain){
        return workers.get(domain);
    }


    HttpClient client(){
        return server.client();
    }


    HttpClient client(SslContext ctx){
        return server.client(ctx);
    }


    File getWorkDir(HWorker worker) {
        return new File("");
    }


    protected abstract IDataProvider getDefDataProvider();


    protected void onAddTempRoute(HPCLTask task){};


    protected static SslContext readSsl(File pubKey, File privKey) throws SSLException {
        return SslContextBuilder.forServer(pubKey, privKey)
                .applicationProtocolConfig(new ApplicationProtocolConfig(
                        ApplicationProtocolConfig.Protocol.ALPN,
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2,
                        ApplicationProtocolNames.HTTP_1_1
                )).build();
    }


    protected static SslContext readSslSafe(File pubKey, File privKey){
        try {
            return readSsl(pubKey, privKey);
        } catch (SSLException e) {
            e.printStackTrace();
        }
        return null;
    }
}