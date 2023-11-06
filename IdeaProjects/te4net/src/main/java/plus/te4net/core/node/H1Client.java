package plus.te4net.core.node;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import plus.te4net.core.api.HttpClient;
import plus.te4net.core.api.IResponse;
import plus.te4net.core.api.IResponseProcessor;
import static plus.te4net.core.node.HServer.SERVER_NAME;


final class H1Client implements ChannelFactory<SocketChannel>, IResponseProcessor, HttpClient {
    private final SslContext sslCtx;
    private final EventLoopGroup group;
    private final boolean useNative;

    H1Client(SslContext ctx, EventLoopGroup group, boolean useNative){
        this.group = group;
        this.useNative = useNative;
        sslCtx = ctx;
    }


    @Override
    public void send(String url, HttpMethod mtd) {
        send(url, mtd, null, this);
    }


    @Override
    public void post(String url, byte[] data){
        send(url, HttpMethod.POST, data, this);
    }


    public void send(String host, String patch, int port, boolean safe, HttpMethod mtd, IResponseProcessor processor, byte[] data){
        new Bootstrap().channelFactory(this).group(group).handler(new H1ClientInitializer(safe?sslCtx:null, processor))
                .connect(host, port).addListener((ChannelFutureListener) future -> {
                    Channel ch = future.channel();
                    HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, mtd,
                            patch, data==null?Unpooled.EMPTY_BUFFER:Unpooled.wrappedBuffer(data));
                    request.headers().set(HttpHeaderNames.HOST, host);
                    applyHeaders(request.headers());
                    ch.writeAndFlush(request);
                });
    }


    private void applyHeaders(HttpHeaders headers){
        headers.set(HttpHeaderNames.SERVER, SERVER_NAME);
        headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        headers.set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
    }


    @Override
    public SocketChannel newChannel() {
        return useNative?new EpollSocketChannel():new NioSocketChannel();
    }


    @Override
    public void handle(IResponse response) {}
}