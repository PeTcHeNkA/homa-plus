package plus.te4net.core.node;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SniHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.Mapping;
import io.netty.util.ReferenceCountUtil;


final class HSInitializer extends ChannelInitializer<SocketChannel> {
    private final HWorkerRepositoryBase repository;

    HSInitializer(HWorkerRepositoryBase repository) {
        this.repository = repository;
    }


    @Override
    protected void initChannel(SocketChannel channel) {
        try {
            new SSLMap(channel.pipeline());
        } catch (Exception e){
            channel.close();
        }
    }


    private final class SSLMap implements Mapping<String, SslContext> {
        private final ChannelPipeline pipeline;
        HEWorkerRepository.HWorkerContext ctx;

        public SSLMap(ChannelPipeline pipeline) {
            (this.pipeline = pipeline).addLast(new Sni2Handler(this));
        }


        @Override
        public SslContext map(String input) {
            if(input == null || (ctx = repository.getFor(input)) == null)return null;
            pipeline.addLast(new H2Proxy(ctx.worker, input));
            return ctx.getCert();
        }
    }


    private static final class Sni2Handler extends SniHandler{
        public Sni2Handler(Mapping<? super String, ? extends SslContext> mapping) {
            super(mapping);
        }


        protected void replaceHandler(ChannelHandlerContext ctx, String hostname, SslContext sslContext) {
            SslHandler sslHandler = null;
            try {
                if(sslContext == null) {
                    ctx.close();
                    return;
                }
                ctx.pipeline().replace(
                        this,
                        SslHandler.class.getName(),
                        sslHandler = newSslHandler(sslContext, ctx.alloc())
                );
                sslHandler = null;
            } finally {
                if (sslHandler != null) ReferenceCountUtil.safeRelease(sslHandler.engine());
            }
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if(!ctx.isRemoved()) ctx.close();
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if(!ctx.isRemoved()) ctx.close();
    }
}