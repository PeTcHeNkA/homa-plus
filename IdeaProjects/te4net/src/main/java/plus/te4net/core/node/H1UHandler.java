package plus.te4net.core.node;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import plus.te4net.core.mapping.HRouteMap;


final class H1UHandler extends AbsH1Handler {
    private final HWorkerRepositoryBase repository;
    private HWorker worker;
    private String domain;

    H1UHandler(HWorkerRepositoryBase repository) {
        this.repository = repository;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        this.ctx = ctx;
        if (!(msg instanceof FullHttpRequest)) {
            send("", HttpResponseStatus.BAD_REQUEST);
            return;
        }
        this.request = (FullHttpRequest) msg;
        domain = request.headers().get("host");
        if(domain==null){
            super.requestError("You're a bot right?");
            return;
        }
        HEWorkerRepository.HWorkerContext ctx0 = repository.getFor(domain);
        if(ctx0==null){
            notFound();
            return;
        }
        worker = ctx0.worker;
        uriParams = new HRouteMap.StrKey(request.uri());
        worker.handle(this);
    }


    @Override
    public HWorker worker() {
        return worker;
    }


    @Override
    public boolean isSafe() {
        return false;
    }


    @Override
    public String getDomain() {
        return domain;
    }


    @Override
    public String toString() {
        return "["+worker+"]:HTTP/1.1 "+method()+" "+path();
    }
}