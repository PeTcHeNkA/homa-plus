package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SNodePacketPlayBase;


public class SPacketChat extends SNodePacketPlayBase {
    private ITextComponent chatComponent;
    private ChatType type;

    public SPacketChat() {}

    public SPacketChat(ITextComponent comp) {
        this(comp, ChatType.SYSTEM);
    }


    public SPacketChat(ITextComponent comp, ChatType type) {
        this.chatComponent = comp;
        this.type = type;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.chatComponent = buf.readTextComponent();
        this.type = ChatType.byId(buf.readByte());
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeTextComponent(this.chatComponent);
        buf.writeByte(this.type.getId());
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);
        NetUtils.writeTextComponent(buf, this.chatComponent);
        buf.writeByte(this.type.getId());
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleChat(this);
    }


    @SideOnly(Side.CLIENT)
    public ITextComponent getChatComponent() {
        return this.chatComponent;
    }


    public boolean isSystem() {
        return this.type == ChatType.SYSTEM || this.type == ChatType.GAME_INFO;
    }


    public ChatType getType() {
        return this.type;
    }
}
