package plus.te4net.core.node;

import io.netty.handler.ssl.SslContext;
import plus.te4net.core.api.IEventHandler;
import plus.te4net.core.api.IHttpClient;
import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.event.EventBus;
import plus.te4net.mds.html.events.VrHtmlEvent;
import plus.te4net.core.mapping.HRouteMap;
import plus.te4net.core.api.IAuthProvider;
import plus.util.cons.Logger;
import java.io.File;


public abstract class HWorker {
    protected final HRouteMap routes = new HRouteMap();
    final boolean allowHttp, hasCert;
    final HWorkerRepositoryBase repository;
    final String mainDomain;

    HWorker(HWorkerRepositoryBase repository, boolean allowHttp, boolean hasCert, String mainDomain) {
        this.repository = repository;
        this.allowHttp = allowHttp;
        this.hasCert = hasCert;
        this.mainDomain = mainDomain;
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


    public final boolean isAllowHttp() {
        return allowHttp;
    }


    public final boolean maybeSafe(){
        return hasCert;
    }


    final void initEventSystem(){
        EventBus bus = new EventBus();
        repository.objBus.put(EventBus.class, this, bus);
        bus.addEventType(
                VrHtmlEvent.class
        );
    }


    public final void addEventType(Class<?> classes){
        repository.objBus.get(EventBus.class, this).addEventType(classes);
    }


    public final void throwEvent(Object obj){
        repository.objBus.get(EventBus.class, this).throwEvent(obj);
    }


    public final <T> void addEventHandler(IEventHandler<T> handler){
        repository.objBus.get(EventBus.class, this).addEventHandler(handler);
    }


    public final <T> void addEventHandler(Class<T> clazz, IEventHandler<T> handler){
        repository.objBus.get(EventBus.class, this).addEventHandler(clazz, handler);
    }


    public File getWorkDir(){
        return repository.getWorkDir(this);
    }


    public String getMainDomain() {
        return mainDomain;
    }


    public String getMainHost(){
        if(hasCert)return "https://"+getMainDomain();
        return "http://"+getMainDomain();
    }


    public IHttpClient client(){
        return repository.client();
    }


    public IHttpClient client(SslContext ctx){
        return repository.client(ctx);
    }


    public IAuthProvider getAuthSystem(){
        return null;
    }


    public static Logger getLogger(){
        return HServer.getLogger();
    }
}