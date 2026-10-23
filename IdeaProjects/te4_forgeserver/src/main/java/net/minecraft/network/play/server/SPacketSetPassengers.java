package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.List;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketSetPassengers extends SPacketPlayBase{
    private int entityId;
    private int[] passengerIds;

    public SPacketSetPassengers() {}


    public SPacketSetPassengers(Entity entity, List<Entity> passengers) {
        this.entityId = entity.getEntityId();
        this.passengerIds = new int[passengers.size()];

        for(int i = 0; i < passengers.size(); ++i) {
            this.passengerIds[i] = passengers.get(i).getEntityId();
        }
    }


    public SPacketSetPassengers(Entity entity) {
        this.entityId = entity.getEntityId();
        List<Entity> passengers = entity.getPassengers();
        this.passengerIds = new int[passengers.size()];

        for(int i = 0; i < passengers.size(); ++i) {
            this.passengerIds[i] = passengers.get(i).getEntityId();
        }
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityId = buf.readVarInt();
        this.passengerIds = buf.readVarIntArray();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.entityId);
        buf.writeVarIntArray(this.passengerIds);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);
        NetUtils.writeVarInt(buf, this.entityId);
        NetUtils.writeVarIntArray(buf, this.passengerIds);
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleSetPassengers(this);
    }


    @SideOnly(Side.CLIENT)
    public int[] getPassengerIds() {
        return this.passengerIds;
    }


    @SideOnly(Side.CLIENT)
    public int getEntityId() {
        return this.entityId;
    }
}