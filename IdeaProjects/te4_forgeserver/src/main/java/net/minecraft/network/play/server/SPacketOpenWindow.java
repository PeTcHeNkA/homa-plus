package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SNodePacketPlayBase;


public class SPacketOpenWindow extends SNodePacketPlayBase {
    private int windowId;
    private String inventoryType;
    private ITextComponent windowTitle;
    private int slotCount;
    private int entityId;

    public SPacketOpenWindow() {}

    public SPacketOpenWindow(int id, String type, ITextComponent title) {
        this(id, type, title, 0);
    }


    public SPacketOpenWindow(int id, String type, ITextComponent title, int count) {
        this.windowId = id;
        this.inventoryType = type;
        this.windowTitle = title;
        this.slotCount = count;
    }


    public SPacketOpenWindow(int id, String type, ITextComponent title, int count, int entity) {
        this(id, type, title, count);
        this.entityId = entity;
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleOpenWindow(this);
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.windowId = buf.readUnsignedByte();
        this.inventoryType = buf.readString(32);
        this.windowTitle = buf.readTextComponent();
        this.slotCount = buf.readUnsignedByte();
        if (this.inventoryType.equals("EntityHorse")) {
            this.entityId = buf.readInt();
        }
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeByte(this.windowId);
        buf.writeString(this.inventoryType);
        buf.writeTextComponent(this.windowTitle);
        buf.writeByte(this.slotCount);
        if (this.inventoryType.equals("EntityHorse")) {
            buf.writeInt(this.entityId);
        }
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        buf.writeByte(this.windowId);
        NetUtils.writeString(buf, this.inventoryType);
        NetUtils.writeTextComponent(buf, this.windowTitle);
        buf.writeByte(this.slotCount);
        if (this.inventoryType.equals("EntityHorse")) {
            buf.writeInt(this.entityId);
        }
    }


    @SideOnly(Side.CLIENT)
    public int getWindowId() {
        return this.windowId;
    }


    @SideOnly(Side.CLIENT)
    public String getGuiId() {
        return this.inventoryType;
    }


    @SideOnly(Side.CLIENT)
    public ITextComponent getWindowTitle() {
        return this.windowTitle;
    }


    @SideOnly(Side.CLIENT)
    public int getSlotCount() {
        return this.slotCount;
    }


    @SideOnly(Side.CLIENT)
    public int getEntityId() {
        return this.entityId;
    }


    @SideOnly(Side.CLIENT)
    public boolean hasSlots() {
        return this.slotCount > 0;
    }
}
