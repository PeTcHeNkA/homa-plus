package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.network.NetHandlerHandshakeMemory;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerHandshakeTCP;
import net.minecraft.util.LazyLoadBase;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import plus.fserver.api.Keep;
import plus.fserver.server.network.ElcaFireWall;


@Keep
public class NetworkSystem {
    private static final Logger LOGGER = LogManager.getLogger();
    @Keep
    public static final LazyLoadBase<NioEventLoopGroup> SERVER_NIO_EVENTLOOP = new LazyLoadBase<NioEventLoopGroup>() {
        protected NioEventLoopGroup load() {
            return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Elca Server #%d").setDaemon(true).setThreadFactory(net.minecraftforge.fml.common.thread.SidedThreadGroups.SERVER).build());
        }
    };

    @Keep
    public static final LazyLoadBase<EpollEventLoopGroup> SERVER_EPOLL_EVENTLOOP = new LazyLoadBase<EpollEventLoopGroup>() {
        protected EpollEventLoopGroup load() {
            return new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Elca Epoll Server #%d").setDaemon(true).setThreadFactory(net.minecraftforge.fml.common.thread.SidedThreadGroups.SERVER).build());
        }
    };

    @Keep
    public static final LazyLoadBase<LocalEventLoopGroup> SERVER_LOCAL_EVENTLOOP = new LazyLoadBase<LocalEventLoopGroup>() {
        protected LocalEventLoopGroup load() {
            return new LocalEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Server IO #%d").setDaemon(true).setThreadFactory(net.minecraftforge.fml.common.thread.SidedThreadGroups.CLIENT).build());
        }
    };


    private final MinecraftServer server;
    @Keep
    public volatile boolean isAlive;
    private final List<ChannelFuture> endpoints = Collections.synchronizedList(Lists.newArrayList());
    private final List<NetworkManager> networkManagers = Collections.synchronizedList(Lists.newArrayList());
    public EventLoopGroup usedPool;
    private ElcaFireWall<Channel> fireWall;

    public NetworkSystem(MinecraftServer server) {
        this.server = server;
        this.isAlive = true;
    }


    public void addEndpoint(InetAddress address, int port) throws IOException {
        if (address instanceof java.net.Inet6Address) System.setProperty("java.net.preferIPv4Stack", "false");
        synchronized (this.endpoints) {
            Class <? extends ServerSocketChannel > oclass;
            LazyLoadBase <? extends EventLoopGroup > lazyloadbase;

            if (Epoll.isAvailable() && this.server.shouldUseNativeTransport()) {
                oclass = EpollServerSocketChannel.class;
                lazyloadbase = SERVER_EPOLL_EVENTLOOP;
                LOGGER.info("Using epoll channel type");
            } else {
                oclass = NioServerSocketChannel.class;
                lazyloadbase = SERVER_NIO_EVENTLOOP;
                LOGGER.info("Using default channel type");
            }

            this.endpoints.add((new ServerBootstrap()).channel(oclass).childHandler(
                    fireWall = new ElcaFireWall<Channel>(lazyloadbase.getValue()) {
                protected void postInit(Channel chan) throws Exception {
                    try {
                        chan.config().setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
                    } catch (ChannelException var3) {}

                    chan.pipeline()
                            .addLast("timeout"     , new ReadTimeoutHandler(FMLNetworkHandler.READ_TIMEOUT))
                            .addLast("legacy_query", new LegacyPingHandler(NetworkSystem.this))
                            .addLast("splitter"    , new NettyVarint21FrameDecoder())
                            .addLast("decoder"     , new NettyPacketDecoder(EnumPacketDirection.SERVERBOUND))
                            .addLast("prepender"   , new NettyVarint21FrameEncoder())
                            .addLast("encoder"     , new NettyPacketEncoder(EnumPacketDirection.CLIENTBOUND));

                    NetworkManager manager = new NetworkManager(EnumPacketDirection.SERVERBOUND);
                    NetworkSystem.this.networkManagers.add(manager);
                    chan.pipeline()
                            .addLast("packet_handler", manager);
                    manager.setNetHandler(new NetHandlerHandshakeTCP(NetworkSystem.this.server, manager));
                }
            }).group(usedPool = lazyloadbase.getValue()).localAddress(address, port).bind().syncUninterruptibly());
        }
    }


    public void softBan(EntityPlayerMP player, long time){
        fireWall.softSet(player, time, false);
    }


    public void softBan(InetAddress address, long time){
        fireWall.softSet(address, time, false);
    }


    public void softAllow(EntityPlayerMP player, long time){
        fireWall.softSet(player, time, true);
    }


    public void softAllow(InetAddress address, long time){
        fireWall.softSet(address, time, true);
    }


    public boolean hasFirewall(){
        return fireWall != null;
    }


    public boolean fireWallEnabled(){
        return fireWall != null && fireWall.isBreakUnknown();
    }


    public void setFireWallEnabled(boolean enabled){
        if(fireWall != null) fireWall.setBreakUnknown(enabled);
    }


    public ElcaFireWall<Channel> fireWall() {
        return fireWall;
    }


    @SideOnly(Side.CLIENT)
    public SocketAddress addLocalEndpoint() {
        ChannelFuture channelfuture;

        synchronized (this.endpoints) {
            channelfuture = (new ServerBootstrap()).channel(LocalServerChannel.class).childHandler(new ChannelInitializer<Channel>() {
                protected void initChannel(Channel chan) throws Exception {
                    NetworkManager manager = new NetworkManager(EnumPacketDirection.SERVERBOUND);
                    manager.setNetHandler(new NetHandlerHandshakeMemory(NetworkSystem.this.server, manager));
                    NetworkSystem.this.networkManagers.add(manager);
                    chan.pipeline().addLast("packet_handler", manager);
                }
            }).group(usedPool = SERVER_NIO_EVENTLOOP.getValue()).localAddress(LocalAddress.ANY).bind().syncUninterruptibly();
            this.endpoints.add(channelfuture);
        }
        return channelfuture.channel().localAddress();
    }


    public void terminateEndpoints() {
        this.isAlive = false;
        if(fireWall != null) fireWall.destroy();
        for (ChannelFuture channelfuture : this.endpoints) {
            try {
                channelfuture.channel().close().sync();
            } catch (InterruptedException var4) {
                LOGGER.error("Interrupted whilst closing channel");
            }
        }
    }


    public void networkTick() {
        synchronized (this.networkManagers) {
            Iterator<NetworkManager> iterator = this.networkManagers.iterator();

            while (iterator.hasNext()) {
                final NetworkManager manager = iterator.next();

                if (!manager.hasNoChannel()) {
                    if (manager.isChannelOpen()) {
                        try {
                            manager.processReceivedPackets();
                        } catch (Exception exception) {
                            if (manager.isLocalChannel()) {
                                CrashReport crashreport = CrashReport.makeCrashReport(exception, "Ticking memory connection");
                                CrashReportCategory crashreportcategory = crashreport.makeCategory("Ticking connection");
                                crashreportcategory.addDetail("Connection", manager::toString);
                                throw new ReportedException(crashreport);
                            }

                            LOGGER.warn("Failed to handle packet for {}", manager.getRemoteAddress(), exception);
                            final TextComponentString textcomponentstring = new TextComponentString("Internal server error");
                            manager.sendPacket(new SPacketDisconnect(textcomponentstring), future -> manager.closeChannel(textcomponentstring));
                            manager.disableAutoRead();
                        }
                    } else {
                        iterator.remove();
                        manager.handleDisconnection();
                    }
                }
            }
        }
    }


    public MinecraftServer getServer() {
        return this.server;
    }
}