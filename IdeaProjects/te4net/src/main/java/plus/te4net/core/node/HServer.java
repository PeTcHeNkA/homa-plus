package plus.te4net.core.node;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.OpenSsl;
import plus.te4net.utl.Logger;
import plus.tson.TsonInt;
import plus.tson.TsonMap;
import java.util.concurrent.ThreadFactory;


public final class HServer implements ThreadFactory {
    static final String SERVER_NAME = "tehnet-v2.1";
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final boolean useNative;
    private ChannelFuture PHTTPS;
    private ChannelFuture PHTTP;

    public HServer(TsonMap config){
        HWorkerRepository repository = new HWorkerRepository(config, this);
        useNative = Epoll.isAvailable();
        if(useNative){
            Logger.get().info("Used [Netty native]");
        } else {
            Logger.get().info("Used [Netty java]");
        }
        if(OpenSsl.isAvailable()){
            Logger.get().info("Used [OpenSSL native]");
        } else {
            Logger.get().info("Used [JDK SSL provider]");
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


    private EventLoopGroup build(int count) {
        if(useNative) return new EpollEventLoopGroup(this);
        else return new NioEventLoopGroup(count, this);
    }


    private Class<? extends ServerSocketChannel> clazz() {
        if(useNative) return EpollServerSocketChannel.class;
        else return NioServerSocketChannel.class;
    }


    private void start(HWorkerRepository repository, int port, boolean safe){
        newThread(()->{
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(clazz())
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(safe?new HSInitializer(repository):new NHInitializer(repository));
            System.out.println("Server at ["+port+"] started.");
            ChannelFuture future = b.bind(port).sync();
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
        return new Thread(r, "hnet");
    }
}