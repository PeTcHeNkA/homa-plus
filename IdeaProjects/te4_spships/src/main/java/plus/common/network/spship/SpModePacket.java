package plus.common.network.spship;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import plus.common.network.BasicPacket;
import plus.common.player.PlayerSpship;


public class SpModePacket extends BasicPacket {

    public boolean mode = false;

    public static class ServerHandler implements IMessageHandler<SpModePacket, SpModePacket> {
        @Override
        public SpModePacket onMessage(SpModePacket message, MessageContext ctx) {
            ((PlayerSpship)ctx.getServerHandler().player).setSpMode(message.mode);
            return null;
        }
    }

    public static class ClientHandler implements IMessageHandler<SpModePacket, SpModePacket> {
        @Override
        public SpModePacket onMessage(SpModePacket message, MessageContext ctx) {
            ((PlayerSpship) Minecraft.getMinecraft().player).setSpMode(message.mode);
            return null;
        }
    }



    public SpModePacket() {}
    public SpModePacket(boolean mode){
        this.mode = mode;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        mode = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(mode);
    }
}