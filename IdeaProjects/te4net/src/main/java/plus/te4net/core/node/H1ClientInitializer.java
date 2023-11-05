package plus.te4net.core.node;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.ssl.SslContext;
import plus.te4net.core.api.IResponseProcessor;


final class H1ClientInitializer extends ChannelInitializer<SocketChannel> {
    private final SslContext sslCtx;
    private final IResponseProcessor processor;

    public H1ClientInitializer(SslContext sslCtx, IResponseProcessor processor) {
        this.sslCtx = sslCtx;
        this.processor = processor;
    }


    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }
        p.addLast(new HttpClientCodec());
        p.addLast(new HttpContentDecompressor());
        p.addLast(new H1ClientHandler(processor));
    }
}