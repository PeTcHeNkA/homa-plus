package plus.te4net.core.node;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import plus.te4net.core.mapping.HRouteMap;


final class H1Handler extends AbsH1Handler {
    private final HWorker worker;
    private final String domain;

    H1Handler(HWorker worker, String domain) {
        this.worker = worker;
        this.domain = domain;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        this.ctx = ctx;
        if (!(msg instanceof FullHttpRequest)) {
            send("", HttpResponseStatus.BAD_REQUEST);
            return;
        }
        this.request = (FullHttpRequest) msg;
        uriParams = new HRouteMap.StrKey(request.uri());
        worker.handle(this);
    }


    @Override
    public byte[] encodeBody() {
        return request.content().array();
    }


    @Override
    public String getDomain() {
        return domain;
    }


    @Override
    public HWorker worker() {
        return worker;
    }
}