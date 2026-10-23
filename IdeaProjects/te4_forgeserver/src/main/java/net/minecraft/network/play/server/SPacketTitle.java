package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.Locale;
import javax.annotation.Nullable;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SNodePacketPlayBase;


public class SPacketTitle extends SNodePacketPlayBase {
    private Type type;
    private ITextComponent message;
    private int fadeInTime;
    private int displayTime;
    private int fadeOutTime;

    public SPacketTitle() {}

    public SPacketTitle(Type type, ITextComponent component) {
        this(type, component, -1, -1, -1);
    }


    public SPacketTitle(int fadeIn, int display, int fadeOut) {
        this(SPacketTitle.Type.TIMES, null, fadeIn, display, fadeOut);
    }


    public SPacketTitle(Type type, @Nullable ITextComponent msg, int fadeIn, int display, int fadeOut) {
        this.type = type;
        this.message = msg;
        this.fadeInTime = fadeIn;
        this.displayTime = display;
        this.fadeOutTime = fadeOut;
    }


    @Override
    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.type = (Type)buf.readEnumValue(Type.class);
        if (this.type == SPacketTitle.Type.TITLE || this.type == SPacketTitle.Type.SUBTITLE || this.type == SPacketTitle.Type.ACTIONBAR) {
            this.message = buf.readTextComponent();
        }

        if (this.type == SPacketTitle.Type.TIMES) {
            this.fadeInTime = buf.readInt();
            this.displayTime = buf.readInt();
            this.fadeOutTime = buf.readInt();
        }
    }


    @Override
    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(this.type);
        if (this.type == SPacketTitle.Type.TITLE || this.type == SPacketTitle.Type.SUBTITLE || this.type == SPacketTitle.Type.ACTIONBAR) {
            buf.writeTextComponent(this.message);
        }

        if (this.type == SPacketTitle.Type.TIMES) {
            buf.writeInt(this.fadeInTime);
            buf.writeInt(this.displayTime);
            buf.writeInt(this.fadeOutTime);
        }
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);
        NetUtils.writeEnumValue(buf, this.type);
        if (this.type == SPacketTitle.Type.TITLE || this.type == SPacketTitle.Type.SUBTITLE || this.type == SPacketTitle.Type.ACTIONBAR) {
            NetUtils.writeTextComponent(buf, this.message);
        }

        if (this.type == SPacketTitle.Type.TIMES) {
            buf.writeInt(this.fadeInTime);
            buf.writeInt(this.displayTime);
            buf.writeInt(this.fadeOutTime);
        }
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleTitle(this);
    }


    @SideOnly(Side.CLIENT)
    public Type getType() {
        return this.type;
    }


    @SideOnly(Side.CLIENT)
    public ITextComponent getMessage() {
        return this.message;
    }


    @SideOnly(Side.CLIENT)
    public int getFadeInTime() {
        return this.fadeInTime;
    }


    @SideOnly(Side.CLIENT)
    public int getDisplayTime() {
        return this.displayTime;
    }


    @SideOnly(Side.CLIENT)
    public int getFadeOutTime() {
        return this.fadeOutTime;
    }


    public static enum Type {
        TITLE,
        SUBTITLE,
        ACTIONBAR,
        TIMES,
        CLEAR,
        RESET;

        private Type() {}

        public static Type byName(String name) {
            Type[] var1 = values();
            for (Type type : var1) {
                if (type.name().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return TITLE;
        }


        public static String[] getNames() {
            Type[] var2 = values();
            String[] values = new String[var2.length];
            int i = 0;

            for (Type type : var2) {
                values[i++] = type.name().toLowerCase(Locale.ROOT);
            }
            return values;
        }
    }
}