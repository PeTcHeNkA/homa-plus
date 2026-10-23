package plus.te4net.core.node;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.stream.ChunkedWriteHandler;


final class H2Proxy extends ApplicationProtocolNegotiationHandler {
    private final HWorker worker;
    private final String domain;

    H2Proxy(HWorker worker, String domain) {
        super(ApplicationProtocolNames.HTTP_1_1);
        this.worker = worker;
        this.domain = domain;
    }


    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
            ctx.pipeline().addLast(
                    Http2FrameCodecBuilder.forServer().build(),
                    new Http2MultiplexHandler(new H2Handler(worker, domain))
            );
            return;
        }
        if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
            ctx.pipeline().addLast(
                    new HttpServerCodec(),
                    new HttpObjectAggregator(worker.repository.maxPostMemory),
                    new ChunkedWriteHandler(),
                    new H1Handler(worker, domain)
            );
            return;
        }
        throw new IllegalStateException("unknown protocol: " + protocol);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if(!ctx.isRemoved()) ctx.close();
    }
}