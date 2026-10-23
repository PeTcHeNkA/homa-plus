package plus.server;

import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import plus.common.network.spship.SpModePacket;
import plus.common.player.PlayerSpship;


public class SpModeServerHandler implements IMessageHandler<SpModePacket, SpModePacket> {
    @Override
    public SpModePacket onMessage(SpModePacket message, MessageContext ctx) {
        ((PlayerSpship) ctx.getServerHandler().player).setSpMode(message.mode);
        return null;
    }
}