package plus.te4net.core.node;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;


final class NHInitializer extends ChannelInitializer<SocketChannel> {
    private final HWorkerRepository repository;

    NHInitializer(HWorkerRepository repository) {
        this.repository = repository;
    }


    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(
                new HttpRequestDecoder(),
                new HttpObjectAggregator(repository.maxPostMemory),
                new HttpResponseEncoder(),
                new ChunkedWriteHandler(),
                new H1UHandler(repository)
        );
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {}
}