package plus.common.network.spship;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import plus.common.network.BasicPacket;
import plus.common.player.PlayerSpship;


public class SpShootPacket extends BasicPacket {
    public float yaw, pith;

    public SpShootPacket(){}

    public SpShootPacket(float yaw, float pith){
        this.yaw = yaw;
        this.pith = pith;
        System.out.println("yaw: "+yaw+", pith: "+pith);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        yaw = buf.readFloat();
        pith = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(yaw);
        buf.writeFloat(pith);
    }
}