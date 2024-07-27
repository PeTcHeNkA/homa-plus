package plus.space.common.net;

import io.netty.util.collection.IntObjectHashMap;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import plus.tson.TsonMap;
import java.util.function.Function;


public class TsonNetHanler implements IMessageHandler<TsonPacket, TsonPacket> {
    private static final IntObjectHashMap<Function<TsonMap,TsonMap>> handlers = new IntObjectHashMap<>();
    static SimpleNetworkWrapper wrapper;

    public static void register(int serial, Function<TsonMap,TsonMap> func){
        handlers.put(serial, func);
    }


    public TsonNetHanler(){}


    public static void init(SimpleNetworkWrapper network, int id){
        wrapper = network;
        network.registerMessage(TsonNetHanler.class, TsonPacket.class, id, Side.SERVER);
        network.registerMessage(TsonNetHanler.class, TsonPacket.class, id, Side.CLIENT);
    }


    @Override
    public TsonPacket onMessage(TsonPacket message, MessageContext ctx) {
        Function<TsonMap, TsonMap> func = handlers.get(message.serial());
        if(func!=null){
            TsonMap result = func.apply(message.getMap());
            if(result != null)return new TsonPacket(message.serial(), result);
        }
        return null;
    }
}