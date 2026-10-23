package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SEntityPacketPlayBase;


public class SPacketEntityAttach extends SEntityPacketPlayBase {
    private int entityId;
    private int vehicleEntityId;

    public SPacketEntityAttach() {}

    public SPacketEntityAttach(Entity entity, @Nullable Entity vehicle) {
        this.entityId = entity.getEntityId();
        this.vehicleEntityId = vehicle != null ? vehicle.getEntityId() : -1;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityId = buf.readInt();
        this.vehicleEntityId = buf.readInt();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeInt(this.entityId);
        buf.writeInt(this.vehicleEntityId);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        buf.writeInt(this.entityId);
        buf.writeInt(this.vehicleEntityId);
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleEntityAttach(this);
    }


    @SideOnly(Side.CLIENT)
    public int getEntityId() {
        return this.entityId;
    }


    @SideOnly(Side.CLIENT)
    public int getVehicleEntityId() {
        return this.vehicleEntityId;
    }
}
