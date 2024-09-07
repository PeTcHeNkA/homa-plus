package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketTabComplete extends SPacketPlayBase {
    private String[] matches;

    public SPacketTabComplete() {}

    public SPacketTabComplete(String[] matches) {
        this.matches = matches;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.matches = new String[buf.readVarInt()];

        for(int i = 0; i < this.matches.length; ++i) {
            this.matches[i] = buf.readString(32767);
        }
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.matches.length);
        String[] var2 = this.matches;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String str = var2[var4];
            buf.writeString(str);
        }
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        String[] matches = this.matches;
        NetUtils.writeVarInt(buf, matches.length);

        for (String match : matches) {
            NetUtils.writeString(buf, match);
        }
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleTabComplete(this);
    }


    @SideOnly(Side.CLIENT)
    public String[] getMatches() {
        return this.matches;
    }
}