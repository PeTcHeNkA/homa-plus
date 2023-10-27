package plus.te4net.core.node;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import plus.te4net.core.api.IHttpClient;
import plus.util.cons.Logger;
import plus.tson.TsonInt;
import plus.tson.TsonMap;
import javax.net.ssl.SSLException;
import java.util.concurrent.ThreadFactory;


public final class HServer implements ThreadFactory, Thread.UncaughtExceptionHandler, ChannelFactory<ServerChannel> {
    static final String SERVER_NAME = "tehnet-v2.1";
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final boolean useNative;
    private int threadNum = 0;
    private ChannelFuture PHTTPS;
    private ChannelFuture PHTTP;
    private SslContext defSslCtx;
    private H1Client defClient;

    public HServer(TsonMap config){
        HWorkerRepository repository = new HWorkerRepository(config, this);
        useNative = Epoll.isAvailable();
        Logger logger = Logger.get();
        if(useNative){
            logger.info("Used [Netty native]");
        } else {
            logger.info("Used [Netty java]");
        }
        if(OpenSsl.isAvailable()){
            logger.info("Used [OpenSSL native]");
        } else {
            logger.info("Used [JDK SSL provider]");
        }
        bossGroup = build(6);
        workerGroup = build(18);
        startHttp(repository, config);
        startHttps(repository, config);
    }


    void stopHttp(){
        PHTTP.channel().close();
    }


    void stopHttps(){
        PHTTPS.channel().close();
    }


    void startHttp(HWorkerRepository repository, TsonMap config){
        start(repository,
                config.getOrDefault("https-port",new TsonInt(443)).getInt(),true
        );
    }


    void startHttps(HWorkerRepository repository, TsonMap config){
        start(repository,
                config.getOrDefault("http-port",new TsonInt(80)).getInt(),false
        );
    }


    IHttpClient client(){
        if(defClient != null)return defClient;
        return defClient = new H1Client(defCliCtx(), workerGroup, useNative);
    }


    IHttpClient client(SslContext ctx){
        return new H1Client(ctx, workerGroup, useNative);
    }


    private EventLoopGroup build(int count) {
        if(useNative) return new EpollEventLoopGroup(this);
        else return new NioEventLoopGroup(count, this);
    }


    private SslContext defCliCtx(){
        if(defSslCtx != null)return defSslCtx;
        try {
            return defSslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }


    private void start(HWorkerRepository repository, int port, boolean safe){
        newThread(()->{
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channelFactory(this)
                    .option(ChannelOption.SO_BACKLOG, 2048)
                    .childHandler(safe?new HSInitializer(repository):new NHInitializer(repository));
            System.out.println("Server at ["+port+"] started.");
            ChannelFuture future = bootstrap.bind(port).sync();

            if(safe){
                PHTTPS = future;
            } else {
                PHTTP = future;
            }
            future.channel().closeFuture().sync();
            System.out.println("Server at ["+port+"] closed.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        }).start();
    }


    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, "hnet-"+(threadNum++));
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(this);
        return thread;
    }


    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.out.println("Error in ["+t+"]: "+e.getClass());
    }


    @Override
    public ServerChannel newChannel() {
        if(useNative) return new EpollServerSocketChannel();
        return new NioServerSocketChannel();
    }
}