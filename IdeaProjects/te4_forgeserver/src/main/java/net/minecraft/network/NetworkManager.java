package net.minecraft.network;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.velocitypowered.natives.compression.VelocityCompressor;
import com.velocitypowered.natives.util.Natives;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import net.minecraft.util.CryptManager;
import net.minecraft.util.ITickable;
import net.minecraft.util.LazyLoadBase;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import plus.fserver.ElcaConfig;
import plus.fserver.api.Keep;
import plus.fserver.server.network.NativeCompressionDecoder;
import plus.fserver.server.network.NativeCompressionEncoder;


public class NetworkManager extends SimpleChannelInboundHandler < Packet<? >> {
    @Keep
    private static final GenericFutureListener[] emptyFuture = new GenericFutureListener[0];
    private static final Logger LOGGER = LogManager.getLogger();
    @Keep
    public static final Marker NETWORK_MARKER = MarkerManager.getMarker("NETWORK");
    @Keep
    public static final Marker NETWORK_PACKETS_MARKER = MarkerManager.getMarker("NETWORK_PACKETS", NETWORK_MARKER);
    @Keep
    public static final AttributeKey<EnumConnectionState> PROTOCOL_ATTRIBUTE_KEY = AttributeKey.<EnumConnectionState>valueOf("protocol");
    @Keep
    public static final LazyLoadBase<NioEventLoopGroup> CLIENT_NIO_EVENTLOOP = new LazyLoadBase<NioEventLoopGroup>()
    {
        protected NioEventLoopGroup load()
        {
            return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Client IO #%d").setDaemon(true).build());
        }
    };
    @Keep
    public static final LazyLoadBase<EpollEventLoopGroup> CLIENT_EPOLL_EVENTLOOP = new LazyLoadBase<EpollEventLoopGroup>()
    {
        protected EpollEventLoopGroup load()
        {
            return new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build());
        }
    };
    @Keep
    public static final LazyLoadBase<LocalEventLoopGroup> CLIENT_LOCAL_EVENTLOOP = new LazyLoadBase<LocalEventLoopGroup>()
    {
        protected LocalEventLoopGroup load()
        {
            return new LocalEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Client IO #%d").setDaemon(true).build());
        }
    };

    private final EnumPacketDirection direction;
    private final Queue<NetworkManager.InboundHandlerTuplePacketListener> outboundPacketsQueue = Queues.newConcurrentLinkedQueue();
    //todo removed. is safe?
    //private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Channel channel;
    private SocketAddress socketAddress;
    private INetHandler packetListener;
    private ITextComponent terminationReason;
    private boolean isEncrypted;
    private boolean disconnected;

    public NetworkManager(EnumPacketDirection packetDirection) {
        this.direction = packetDirection;
    }


    @Keep
    public EnumPacketDirection getDirection() {
        return this.direction;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.channel = ctx.channel();
        this.socketAddress = this.channel.remoteAddress();
        try {
            this.setConnectionState(EnumConnectionState.HANDSHAKING);
        } catch (Throwable throwable) {
            LOGGER.fatal(throwable);
        }
    }


    @Keep
    public void setConnectionState(EnumConnectionState newState) {
        this.channel.attr(PROTOCOL_ATTRIBUTE_KEY).set(newState);
        this.channel.config().setAutoRead(true);
    }


    @Override
    public void channelInactive(ChannelHandlerContext p_channelInactive_1_) throws Exception {
        this.closeChannel(new TextComponentTranslation("disconnect.endOfStream"));
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext p_exceptionCaught_1_, Throwable throwable) throws Exception {
        TextComponentTranslation textcomponenttranslation;

        if (throwable instanceof TimeoutException) {
            textcomponenttranslation = new TextComponentTranslation("disconnect.timeout");
        } else {
            textcomponenttranslation = new TextComponentTranslation("disconnect.genericReason", "Internal Exception: " + throwable);
        }

        LOGGER.debug(textcomponenttranslation.getUnformattedText(), throwable);
        this.closeChannel(textcomponenttranslation);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet<?> packet) throws Exception {
        if (this.channel.isOpen()) {
            try {
                ((Packet<INetHandler>)packet).processPacket(this.packetListener);
            } catch (ThreadQuickExitException ignored) {
                ;
            }
        }
    }


    @Keep
    public void setNetHandler(INetHandler handler) {
        Validate.notNull(handler, "packetListener");
        LOGGER.debug("Set listener of {} to {}", this, handler);
        this.packetListener = handler;
    }


    @Keep
    public void sendPacket(Packet<?> packetIn) {
        if (this.isChannelOpen()) {
            this.flushOutboundQueue();
            this.dispatchPacket(packetIn, null);
        } else {
            //this.readWriteLock.writeLock().lock();
            try {
                this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packetIn, emptyFuture));
            } finally {
                //this.readWriteLock.writeLock().unlock();
            }
        }
    }


    @Keep
    public void sendPacket(Packet<?> packetIn, GenericFutureListener <? extends Future <? super Void >> listener, GenericFutureListener <? extends Future <? super Void >> ... listeners) {
        if (this.isChannelOpen()) {
            this.flushOutboundQueue();
            this.dispatchPacket(packetIn, ArrayUtils.add(listeners, 0, listener));
        }
        else {
            //this.readWriteLock.writeLock().lock();
            try {
                this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packetIn, ArrayUtils.add(listeners, 0, listener)));
            }
            finally {
                //this.readWriteLock.writeLock().unlock();
            }
        }
    }


    private void dispatchPacket(final Packet<?> inPacket, @Nullable final GenericFutureListener <? extends Future <? super Void >> [] futureListeners) {
        final EnumConnectionState state = EnumConnectionState.getFromPacket(inPacket);
        boolean flag = channel.attr(PROTOCOL_ATTRIBUTE_KEY).get() != state
                && !( inPacket instanceof FMLProxyPacket);
        if (flag) this.channel.config().setAutoRead(false);

        if (this.channel.eventLoop().inEventLoop()) {
            if (flag) this.setConnectionState(state);
            ChannelFuture channelfuture = this.channel.writeAndFlush(inPacket);

            if (futureListeners != null) {
                channelfuture.addListeners(futureListeners);
            }
            channelfuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } else {
            this.channel.eventLoop().execute(() -> {
                if (flag) NetworkManager.this.setConnectionState(state);
                ChannelFuture channelfuture1 = NetworkManager.this.channel.writeAndFlush(inPacket);

                if (futureListeners != null) {
                    channelfuture1.addListeners(futureListeners);
                }
                channelfuture1.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            });
        }
    }


    @Keep
    public void flushOutboundQueue() {
        NetworkManager.InboundHandlerTuplePacketListener listener = this.outboundPacketsQueue.poll();
        while (listener != null){
            this.dispatchPacket(listener.packet, listener.futureListeners);
            listener = this.outboundPacketsQueue.poll();
        }
    }


    @Keep
    public void processReceivedPackets() {
        if(isChannelOpen()) this.flushOutboundQueue();
        if (this.packetListener instanceof ITickable) {
            ((ITickable)this.packetListener).update();
        }
        if (this.channel != null) {
            this.channel.flush();
        }
    }


    @Keep
    public SocketAddress getRemoteAddress() {
        return this.socketAddress;
    }


    @Keep
    public void closeChannel(ITextComponent message) {
        if (this.channel.isOpen()) {
            this.channel.close().awaitUninterruptibly();
            this.terminationReason = message;
        }
    }


    @Keep
    public boolean isLocalChannel() {
        return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
    }


    @SideOnly(Side.CLIENT)
    public static NetworkManager createNetworkManagerAndConnect(InetAddress address, int serverPort, boolean useNativeTransport) {
        if (address instanceof java.net.Inet6Address) System.setProperty("java.net.preferIPv4Stack", "false");
        final NetworkManager networkmanager = new NetworkManager(EnumPacketDirection.CLIENTBOUND);
        Class <? extends SocketChannel > oclass;
        LazyLoadBase <? extends EventLoopGroup > lazyloadbase;
        if (Epoll.isAvailable() && useNativeTransport) {
            oclass = EpollSocketChannel.class;
            lazyloadbase = CLIENT_EPOLL_EVENTLOOP;
        } else {
            oclass = NioSocketChannel.class;
            lazyloadbase = CLIENT_NIO_EVENTLOOP;
        }

        (new Bootstrap()).group(lazyloadbase.getValue()).handler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel) throws Exception {
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
                } catch (ChannelException ignored) {
                    ;
                }
                channel.pipeline()
                        .addLast("timeout", new ReadTimeoutHandler(30))
                        .addLast("splitter", new NettyVarint21FrameDecoder())
                        .addLast("decoder", new NettyPacketDecoder(EnumPacketDirection.CLIENTBOUND))
                        .addLast("prepender", new NettyVarint21FrameEncoder())
                        .addLast("encoder", new NettyPacketEncoder(EnumPacketDirection.SERVERBOUND))
                        .addLast("packet_handler", networkmanager);
            }
        }).channel(oclass).connect(address, serverPort).syncUninterruptibly();
        return networkmanager;
    }


    @SideOnly(Side.CLIENT)
    public static NetworkManager provideLocalClient(SocketAddress address) {
        final NetworkManager networkmanager = new NetworkManager(EnumPacketDirection.CLIENTBOUND);
        (new Bootstrap()).group(CLIENT_LOCAL_EVENTLOOP.getValue()).handler(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast("packet_handler", networkmanager);
            }
        }).channel(LocalChannel.class).connect(address).syncUninterruptibly();
        return networkmanager;
    }


    @Keep
    public void enableEncryption(SecretKey key) {
        this.isEncrypted = true;
        this.channel.pipeline().addBefore("splitter", "decrypt", new NettyEncryptingDecoder(CryptManager.createNetCipherInstance(2, key)));
        this.channel.pipeline().addBefore("prepender", "encrypt", new NettyEncryptingEncoder(CryptManager.createNetCipherInstance(1, key)));
    }


    //todo removed. is safe?
    //@SideOnly(Side.CLIENT)
    public boolean isEncrypted() {
        return this.isEncrypted;
    }


    @Keep
    public boolean isChannelOpen() {
        return this.channel != null && this.channel.isOpen();
    }


    @Keep
    public boolean hasNoChannel() {
        return this.channel == null;
    }


    @Keep
    public INetHandler getNetHandler() {
        return this.packetListener;
    }


    @Keep
    public ITextComponent getExitMessage() {
        return this.terminationReason;
    }


    @Keep
    public void disableAutoRead() {
        this.channel.config().setAutoRead(false);
    }


    @Keep
    public void setCompressionThreshold(int threshold) {
        //Natives.compress.get().create(threshold).
        if (threshold >= 0) {
            VelocityCompressor compress = Natives.compress.get().create(ElcaConfig.NETWORK_COMPRESSION_LVL);
            if (this.channel.pipeline().get("decompress") instanceof NativeCompressionDecoder) {
                ((NativeCompressionDecoder)this.channel.pipeline().get("decompress")).setCompressionThreshold(threshold);
            } else {
                this.channel.pipeline().addBefore("decoder", "decompress",
                        //new NettyCompressionDecoder(threshold)
                        new NativeCompressionDecoder(compress, threshold)
                );
            }
            if (this.channel.pipeline().get("compress") instanceof NativeCompressionEncoder) {
                ((NativeCompressionEncoder)this.channel.pipeline().get("compress"))
                        .setCompressionThreshold(threshold);
            } else {
                this.channel.pipeline().addBefore("encoder", "compress",
                        //new NettyCompressionEncoder(threshold)
                        new NativeCompressionEncoder(threshold, compress)
                );
            }
        } else {
            if (this.channel.pipeline().get("decompress") instanceof NettyCompressionDecoder) {
                this.channel.pipeline().remove("decompress");
            }
            if (this.channel.pipeline().get("compress") instanceof NettyCompressionEncoder) {
                this.channel.pipeline().remove("compress");
            }
        }
    }


    @Keep
    public void handleDisconnection() {
        if (this.channel != null && !this.channel.isOpen()) {
            if (this.disconnected) {
                LOGGER.warn("handleDisconnection() called twice");
            }
            else {
                this.disconnected = true;
                if (this.getExitMessage() != null) {
                    this.getNetHandler().onDisconnect(this.getExitMessage());
                } else if (this.getNetHandler() != null) {
                    this.getNetHandler().onDisconnect(new TextComponentTranslation("multiplayer.disconnect.generic"));
                }
            }
        }
    }


    @Keep
    public Channel channel() {
        return channel;
    }


    @Keep
    public static class InboundHandlerTuplePacketListener {
        private final Packet<?> packet;
        private final GenericFutureListener <? extends Future <? super Void >> [] futureListeners;

        public InboundHandlerTuplePacketListener(Packet<?> inPacket, GenericFutureListener <? extends Future <? super Void >> ... inFutureListeners) {
            this.packet = inPacket;
            this.futureListeners = inFutureListeners;
        }
    }


    public void exec(Runnable runnable){
        channel.eventLoop().execute(runnable);
    }


    public void addToQueue(Packet<?> packetIn){
        //this.readWriteLock.writeLock().lock();
        try {
            this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packetIn, emptyFuture));
        } finally {
            //this.readWriteLock.writeLock().unlock();
        }
    }



    public void sendPacketSync(Packet<?> packetIn){
        if (this.isChannelOpen()) {
            flushOutboundQueue();
            dispatchPacketE(packetIn, null);
        } else {
            //this.readWriteLock.writeLock().lock();
            try {
                this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packetIn, emptyFuture));
            } finally {
                //this.readWriteLock.writeLock().unlock();
            }
        }
    }


    @Keep
    public void sendPacketE(Packet<?> packetIn) {
        if (this.isChannelOpen()) {
            channel.eventLoop().execute(() -> {
                flushOutboundQueue();
                dispatchPacketE(packetIn, null);
            });
        } else {
            //this.readWriteLock.writeLock().lock();
            try {
        this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packetIn, emptyFuture));
            } finally {
                //this.readWriteLock.writeLock().unlock();
            }
        }
    }


    public void addToQueueE(Packet<?> packetIn){
        this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packetIn, emptyFuture));
    }


    private void dispatchPacketE(final Packet<?> inPacket, @Nullable final GenericFutureListener <? extends Future <? super Void >> [] listeners) {
        final EnumConnectionState state = EnumConnectionState.getFromPacket(inPacket);
        final EnumConnectionState state1 = channel.attr(PROTOCOL_ATTRIBUTE_KEY).get();
        if (state != state1) {
            setConnectionState(state);
            channel.config().setAutoRead(false);
        }
        ChannelFuture future = channel.writeAndFlush(inPacket);
        if (listeners != null)
            future.addListeners(listeners);

        future.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }
}