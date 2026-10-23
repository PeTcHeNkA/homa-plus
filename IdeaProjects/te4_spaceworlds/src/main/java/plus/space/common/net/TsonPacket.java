package plus.space.common.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.security.ClassManager;


public class TsonPacket implements IMessage, ClassManager {
    private int serial;
    private TsonMap map;
    private String res;
    public TsonPacket(){}

    public TsonPacket(int serial, String res){
        this.serial = serial;
        this.res = res;
    }

    public TsonPacket(int serial, TsonMap map){
        this.serial = serial;
        this.map = map;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        serial = buf.readShort();
        String str = ByteBufUtils.readUTF8String(buf);
        map = new TsonMap(this, str);
    }


    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(serial);
        ByteBufUtils.writeUTF8String(buf, res==null?map.toString():res);
    }


    @Override
    public Class<?> forName(String clazz) {
        return TsonObj.class;
    }


    protected int serial(){
        return serial;
    }


    protected TsonMap getMap() {
        return map;
    }


    public void sendToServer(){
        TsonNetHanler.wrapper.sendToServer(this);
    }


    public void sendTo(EntityPlayerMP player){
        TsonNetHanler.wrapper.sendTo(this, player);
    }
}