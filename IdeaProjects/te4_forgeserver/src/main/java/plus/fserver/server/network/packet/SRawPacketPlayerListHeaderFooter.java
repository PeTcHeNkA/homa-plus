package plus.fserver.server.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketPlayerListHeaderFooter;
import net.minecraft.util.text.ITextComponent;
import plus.fserver.server.network.NetUtils;
import java.io.IOException;


public class SRawPacketPlayerListHeaderFooter extends SPacketPlayBase {
    private String header;
    private String footer;

    public SRawPacketPlayerListHeaderFooter() {}

    public SRawPacketPlayerListHeaderFooter(String header, String footer){
        if(header == null)header = "{\"text\":\"\"}";
        if(footer == null)footer = "{\"text\":\"\"}";
        this.header = header;
        this.footer = footer;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeString(this.header);
        buf.writeString(this.footer);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeString(buf, this.header);
        NetUtils.writeString(buf, this.footer);
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handlePlayerListHeaderFooter(new SPacketPlayerListHeaderFooter(
                ITextComponent.Serializer.jsonToComponent(header),
                ITextComponent.Serializer.jsonToComponent(footer)
        ));
    }
}