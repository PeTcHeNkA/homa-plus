package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SEntityPacketPlayBase;


public class SPacketCollectItem extends SEntityPacketPlayBase {
    private int collectedItemEntityId;
    private int entityId;
    private int collectedQuantity;

    public SPacketCollectItem() {}

    public SPacketCollectItem(int cItemId, int entityId, int quantity) {
        this.collectedItemEntityId = cItemId;
        this.entityId = entityId;
        this.collectedQuantity = quantity;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.collectedItemEntityId = buf.readVarInt();
        this.entityId = buf.readVarInt();
        this.collectedQuantity = buf.readVarInt();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.collectedItemEntityId);
        buf.writeVarInt(this.entityId);
        buf.writeVarInt(this.collectedQuantity);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeVarInt(buf, this.collectedItemEntityId);
        NetUtils.writeVarInt(buf, this.entityId);
        NetUtils.writeVarInt(buf, this.collectedQuantity);
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleCollectItem(this);
    }


    @SideOnly(Side.CLIENT)
    public int getCollectedItemEntityID() {
        return this.collectedItemEntityId;
    }


    @SideOnly(Side.CLIENT)
    public int getEntityID() {
        return this.entityId;
    }


    @SideOnly(Side.CLIENT)
    public int getAmount() {
        return this.collectedQuantity;
    }
}
