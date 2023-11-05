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
    protected void initChannel(SocketChannel ch) {
        try {
            new SSLMap(ch.pipeline());
        } catch (Exception e){
            ch.close();
        }
    }


    private final class SSLMap implements Mapping<String, SslContext> {
        private final ChannelPipeline ph;
        HEWorkerRepository.HWorkerContext ctx;
        public SSLMap(ChannelPipeline ph) {
            this.ph = ph;
            ph.addLast(new Sni2Handler(this));
        }

        @Override
        public SslContext map(String input) {
            if(input==null) return null;
            ctx = repository.getFor(input);
            if(ctx==null)return null;
            ph.addLast(new H2Proxy(ctx.worker, input));
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
                if(sslContext==null) {
                    ctx.close();
                    return;
                }
                sslHandler = newSslHandler(sslContext, ctx.alloc());
                ctx.pipeline().replace(this, SslHandler.class.getName(), sslHandler);
                sslHandler = null;
            } finally {
                if (sslHandler != null) {
                    ReferenceCountUtil.safeRelease(sslHandler.engine());
                }
            }
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {}
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {}
}