package plus.fserver.server.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import plus.fserver.server.network.NetUtils;
import java.io.IOException;


public class SRawPacketChat extends SPacketPlayBase {
    private String text;
    private ChatType type;

    public SRawPacketChat() {}

    public SRawPacketChat(String comp) {
        this(comp, ChatType.SYSTEM);
    }


    public SRawPacketChat(String text, ChatType type) {
        this.text = text;
        this.type = type;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        NetUtils.writeString(buf, text);
        buf.writeByte(this.type.getId());
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeString(buf, text);
        buf.writeByte(this.type.getId());
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleChat(new SPacketChat(
                ITextComponent.Serializer.jsonToComponent(text), this.type
        ));
    }


    public boolean isSystem() {
        return this.type == ChatType.SYSTEM || this.type == ChatType.GAME_INFO;
    }


    public ChatType getType() {
        return this.type;
    }
}