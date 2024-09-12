package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketPlayerListHeaderFooter extends SPacketPlayBase {
    private ITextComponent header;
    private ITextComponent footer;

    public SPacketPlayerListHeaderFooter() {}

    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.header = buf.readTextComponent();
        this.footer = buf.readTextComponent();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeTextComponent(this.header);
        buf.writeTextComponent(this.footer);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeTextComponent(buf, this.header);
        NetUtils.writeTextComponent(buf, this.footer);
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handlePlayerListHeaderFooter(this);
    }


    @SideOnly(Side.CLIENT)
    public ITextComponent getHeader() {
        return this.header;
    }


    @SideOnly(Side.CLIENT)
    public ITextComponent getFooter() {
        return this.footer;
    }
}