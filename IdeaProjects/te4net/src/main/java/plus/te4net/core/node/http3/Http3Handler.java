package plus.te4net.core.node.http3;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.incubator.codec.http3.*;
import io.netty.incubator.codec.quic.*;
import io.netty.util.ReferenceCountUtil;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class Http3Handler {
    private static final byte[] CONTENT = "Hello World!\r\n".getBytes();
    private final ChannelHandler codec;

    public static void main(String[] args) throws SSLException, InterruptedException {
        System.out.println("NEN!");
        QuicSslContext ssl = readSsl(new File("C:\\Users\\Te4hnoPlus\\IdeaProjects\\te4net\\domains\\lochost.ru\\lochost.ru.ser"),
                new File("C:\\Users\\Te4hnoPlus\\IdeaProjects\\te4net\\domains\\lochost.ru\\lochost.ru.key"));

        Http3Handler han = new Http3Handler(ssl);

        han.start();
    }

    protected static QuicSslContext readSsl(File pubKey, File privKey) throws SSLException {
        return QuicSslContextBuilder.forServer(privKey, null, pubKey).build();
//                .applicationProtocolConfig(new ApplicationProtocolConfig(
//                        ApplicationProtocolConfig.Protocol.ALPN,
//                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
//                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
//                        ApplicationProtocolNames.HTTP_2,
//                        ApplicationProtocolNames.HTTP_1_1
//                )).build();
    }

    public Http3Handler(QuicSslContext sslContext){
//        QuicSslContext sslContext = QuicSslContextBuilder.forServer(cert.key(), null, cert.cert())
//                .applicationProtocols(Http3.supportedApplicationProtocols()).build();


        codec = Http3.newQuicServerCodecBuilder()
                .sslContext(sslContext)
                .maxIdleTimeout(5000, TimeUnit.MILLISECONDS)
                .initialMaxData(10000000)
                .initialMaxStreamDataBidirectionalLocal(1000000)
                .initialMaxStreamDataBidirectionalRemote(1000000)
                .initialMaxStreamsBidirectional(100)
                .tokenHandler(InsecureQuicTokenHandler.INSTANCE)
                .handler(new ChannelInitializer<QuicChannel>() {
                    @Override
                    protected void initChannel(QuicChannel ch) {
                        // Called for each connection
                        ch.pipeline().addLast(new Http3ServerConnectionHandler(
                                new ChannelInitializer<QuicStreamChannel>() {
                                    // Called for each request-stream,
                                    @Override
                                    protected void initChannel(QuicStreamChannel ch) {
                                        ch.pipeline().addLast(new Http3RequestStreamInboundHandler() {

                                            @Override
                                            protected void channelRead(
                                                    ChannelHandlerContext ctx, Http3HeadersFrame frame) {
                                                ReferenceCountUtil.release(frame);
                                            }

                                            @Override
                                            protected void channelRead(
                                                    ChannelHandlerContext ctx, Http3DataFrame frame) {
                                                ReferenceCountUtil.release(frame);
                                            }

                                            @Override
                                            protected void channelInputClosed(ChannelHandlerContext ctx) {
                                                Http3HeadersFrame headersFrame = new DefaultHttp3HeadersFrame();
                                                headersFrame.headers().status("404");
                                                headersFrame.headers().add("server", "netty");
                                                headersFrame.headers().addInt("content-length", CONTENT.length);
                                                ctx.write(headersFrame);
                                                ctx.writeAndFlush(new DefaultHttp3DataFrame(
                                                                Unpooled.wrappedBuffer(CONTENT)))
                                                        .addListener(QuicStreamChannel.SHUTDOWN_OUTPUT);
                                            }
                                        });
                                    }
                                }));
                    }
                }).build();



    }

    void start() throws InterruptedException {
        NioEventLoopGroup group = null;
        try {
            group = new NioEventLoopGroup(4);
            Bootstrap bs = new Bootstrap();
            Channel channel = bs.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(codec)
                    .bind(new InetSocketAddress(443)).sync().channel();
            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }

    }
}
