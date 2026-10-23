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
    public void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        if (sslCtx != null)
            pipeline.addLast(sslCtx.newHandler(channel.alloc()));

        pipeline.addLast(
                new HttpClientCodec(),
                new HttpContentDecompressor(),
                new H1ClientHandler(processor)
        );
    }
}