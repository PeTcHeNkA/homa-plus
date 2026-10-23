package plus.fserver.server.network;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Packet;


public interface IPacketNodeQueue {
    Packet<?> packet();
    default GenericFutureListener<? extends Future<? super Void >>[] future(){
        return null;
    };
}