package plus.fserver.server.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.util.text.ITextComponent;
import plus.fserver.server.network.NetUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import net.minecraft.network.play.server.SPacketTitle.Type;


public class SRawPacketTitle extends SNodePacketPlayBase {
    private Type type;
    private String message;
    private int fadeInTime;
    private int displayTime;
    private int fadeOutTime;

    public SRawPacketTitle() {}

    public SRawPacketTitle(Type type, String component) {
        this(type, component, -1, -1, -1);
    }


    public SRawPacketTitle(int fadeIn, int display, int fadeOut) {
        this(Type.TIMES, null, fadeIn, display, fadeOut);
    }


    public SRawPacketTitle(Type type, String msg, int fadeIn, int display, int fadeOut) {
        this.type = type;
        this.message = msg;
        this.fadeInTime = fadeIn;
        this.displayTime = display;
        this.fadeOutTime = fadeOut;
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);
        NetUtils.writeEnumValue(buf, this.type);
        if (this.type == Type.TITLE || this.type == Type.SUBTITLE || this.type == Type.ACTIONBAR) {
            NetUtils.writeString(buf, this.message);
        }
        if (this.type == Type.TIMES) {
            buf.writeInt(this.fadeInTime);
            buf.writeInt(this.displayTime);
            buf.writeInt(this.fadeOutTime);
        }
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        throw new UnsupportedEncodingException();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        throw new UnsupportedEncodingException();
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        ITextComponent text = null;
        if(message != null){
            text = ITextComponent.Serializer.jsonToComponent(message);
        }
        proc.handleTitle(new SPacketTitle(
                type, text, fadeInTime, displayTime, fadeOutTime
        ));
    }
}