package plus.te4net.core.node;

import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.api.PublicUsage;
import plus.te4net.core.api.IDataProvider;
import plus.tson.TsonMap;

import java.io.File;
import java.io.InputStream;


@PublicUsage
public abstract class TehnetSimple extends HServer{
    protected final TsonMap config;
    private final Repo repo = new Repo();
    private final HWorker worker;
    private HEWorkerRepository.HWorkerContext defCtx;
    private IRequestProcessor defHandler = RequestSession::notFound;

    private final class Repo extends HWorkerRepositoryBase{
        public Repo() {
            super(TehnetSimple.this, TehnetSimple.this.config);
        }


        @Override
        protected IDataProvider getDefDataProvider() {
            return TehnetSimple.this.getDefDataProvider();
        }


        @Override
        protected HEWorkerRepository.HWorkerContext getFor(String domain) {
            return workers.getOrDefault(domain, defCtx);
        }
    }

    private final class Worker extends HWorker {
        public Worker(boolean allowHttp, boolean hasCert, String mainDomain) {
            super(repo, allowHttp, hasCert, mainDomain);
        }


        @Override
        public void handle(RequestSession session) {
            IRequestProcessor processor = routes.get(session);
            if(processor == null){
                defHandler.handle(session);
            } else {
                try {
                    processor.handle(session);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public TehnetSimple(int threads, TsonMap config) {
        super(threads);
        this.config = config;
        worker = new Worker(true, false, "tehnet");
    }


    public TehnetSimple useDefault(){
        defCtx = new HEWorkerRepository.HWorkerContext(worker);
        return this;
    }


    public TehnetSimple useDomain(String domain){
        HEWorkerRepository.HWorkerContext ctx = new HEWorkerRepository.HWorkerContext(worker);
        repo.workers.putSafe(domain, ctx);
        if(defCtx==null){
            defCtx = ctx;
        }
        return this;
    }


    public TehnetSimple useDomain(String domain, File publicKey, File privateKey){
        HEWorkerRepository.HWorkerContext ctx = new HEWorkerRepository.HWorkerContext(worker,publicKey,privateKey);
        repo.workers.putSafe(domain, ctx);
        if(defCtx==null){
            defCtx = ctx;
        }
        return this;
    }


    public TehnetSimple addRoute(String patch, IRequestProcessor proc){
        worker.addRoute(patch, proc);
        return this;
    }


    public TehnetSimple setDefault(IRequestProcessor proc){
        defHandler = proc;
        return this;
    }


    public final TehnetSimple startHttp(int port){
        start(repo, port, false);
        return this;
    }


    public final TehnetSimple startHttps(int port){
        start(repo, port, true);
        return this;
    }


    public TehnetSimple start(){
        return startHttp(80).startHttps(443);
    }


    protected abstract IDataProvider getDefDataProvider();
}