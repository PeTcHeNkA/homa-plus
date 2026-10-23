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
import plus.te4net.core.api.HttpClient;
import plus.util.cons.Logger;
import plus.tson.TsonMap;
import javax.net.ssl.SSLException;
import java.util.concurrent.ThreadFactory;


public class HServer implements ThreadFactory, Thread.UncaughtExceptionHandler, ChannelFactory<ServerChannel> {
    static final String SERVER_NAME = "tehnet-v2.2";
    private static Logger logger;
    private final EventLoopGroup bossGroup;
    final EventLoopGroup workerGroup;
    private final boolean useNative;
    private int threadNum = 0;
    private ChannelFuture PHTTPS;
    private ChannelFuture PHTTP;
    private SslContext defSslCtx;
    private H1Client defClient;

    public HServer(TsonMap config){
        this(config.getOrDefaultInt("threads", 18));
        startRepo(config, new HEWorkerRepository(config, this));


    }


    public HServer(){
        this(18);
    }


    public HServer(int threads){
        if(threads<6){
            threads = 6;
        }
        logger = Logger.get("tehnet");
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
        bossGroup = build(Math.round(threads/3f));
        workerGroup = build(threads);
    }


    final void startRepo(TsonMap config, HWorkerRepositoryBase repository){
        startHttp(repository, config);
        startHttps(repository, config);
    }


    final void stopHttp(){
        PHTTP.channel().close();
    }


    final void stopHttps(){
        PHTTPS.channel().close();
    }


    final void startHttp(HWorkerRepositoryBase repository, TsonMap config){
        start(repository,
                config.getOrDefaultInt("https-port",443),true
        );
    }


    final void startHttps(HWorkerRepositoryBase repository, TsonMap config){
        start(repository,
                config.getOrDefaultInt("http-port",80),false
        );
    }


    public final HttpClient client(){
        if(defClient != null)return defClient;
        return defClient = new H1Client(defCliCtx(), workerGroup, useNative);
    }


    public final HttpClient client(SslContext ctx){
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


    final void start(HWorkerRepositoryBase repository, int port, boolean safe){
        newThread(()->{
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channelFactory(this)
                    .option(ChannelOption.SO_BACKLOG, 2048)
                    .childHandler(safe?new HSInitializer(repository):new NHInitializer(repository));
            Logger.get().info("Server at ["+port+"] started.");
            ChannelFuture future = bootstrap.bind(port).sync();

            if(safe){
                PHTTPS = future;
            } else {
                PHTTP = future;
            }
            future.channel().closeFuture().sync();
            Logger.get().info("Server at ["+port+"] closed.");
            Thread.currentThread().stop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        }).start();
    }


    @Override
    public final Thread newThread(Runnable r) {
        Thread thread = new Thread(r, "hnet-"+(threadNum++));
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(this);
        return thread;
    }


    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Logger.get().info("Error in ["+t+"]: "+e.getClass());
    }


    @Override
    public final ServerChannel newChannel() {
        if(useNative) return new EpollServerSocketChannel();
        return new NioServerSocketChannel();
    }


    public static Logger getLogger(){
        return HServer.logger;
    }


    public static void preLoad(Class<?>... classes){}
}