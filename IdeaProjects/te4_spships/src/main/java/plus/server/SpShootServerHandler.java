package plus.server;

import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import plus.common.network.spship.SpShootPacket;
import plus.common.player.PlayerSpship;


public class SpShootServerHandler implements IMessageHandler<SpShootPacket, SpShootPacket> {
    @Override
    public SpShootPacket onMessage(SpShootPacket message, MessageContext ctx) {
        ((PlayerSpship) ctx.getServerHandler().player).shoot(message.yaw, message.pith);
        return null;
    }
}
