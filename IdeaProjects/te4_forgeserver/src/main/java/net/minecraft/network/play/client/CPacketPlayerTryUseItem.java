package net.minecraft.network.play.client;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.EnumHand;
import plus.fserver.server.network.NetUtils;


public class CPacketPlayerTryUseItem implements Packet<INetHandlerPlayServer> {
    private EnumHand hand;

    public CPacketPlayerTryUseItem() {}

    public CPacketPlayerTryUseItem(EnumHand hand) {
        this.hand = hand;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.hand = buf.readEnumValue(EnumHand.class);
    }


    @Override
    public void readPacketData(ByteBuf buf) throws IOException {
        this.hand = NetUtils.readEnumValueFast(buf, EnumHand.VALUES);
    }


    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(this.hand);
    }


    @Override
    public void processPacket(INetHandlerPlayServer proc) {
        proc.processTryUseItem(this);
    }


    public EnumHand getHand() {
        return this.hand;
    }
}
