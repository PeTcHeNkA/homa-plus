package plus.common.network.entity;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.common.mashine.inv.IPowerContainer;
import plus.common.network.BasicPacket;


public class SyncCraftPow extends BasicPacket {
    @SideOnly(Side.CLIENT)
    public static IPowerContainer current = null;
    @SideOnly(Side.CLIENT)
    private static int editAuto = -1;
    @SideOnly(Side.CLIENT)
    public static void setCurrent(IPowerContainer cur){
        if(editAuto!=-1) {
            cur.setPower(editAuto);
            editAuto = -1;
        } else {
            current = cur;
        }
    }

    public int power = 1;

    public SyncCraftPow() {}

    public SyncCraftPow(int power){
        this.power = power;
    }


    public static class ClientHandler implements IMessageHandler<SyncCraftPow, SyncCraftPow> {
        @Override
        public SyncCraftPow onMessage(SyncCraftPow msg, MessageContext ctx) {
            if(current != null){
                current.setPower(msg.power);
                current = null;
            } else {
                editAuto = msg.power;
            }
            return null;
        }
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        power = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(power);
    }
}
