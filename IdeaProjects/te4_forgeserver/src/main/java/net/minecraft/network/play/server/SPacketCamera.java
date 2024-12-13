package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;


public class SPacketCamera implements Packet<INetHandlerPlayClient> {
    public int entityId;

    public SPacketCamera() {}

    public SPacketCamera(int id){
        this.entityId = id;
    }

    public SPacketCamera(Entity entity) {
        this.entityId = entity.getEntityId();
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityId = buf.readVarInt();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.entityId);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);
        NetUtils.writeVarInt(buf, this.entityId);
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleCamera(this);
    }


    @Nullable
    @SideOnly(Side.CLIENT)
    public Entity getEntity(World world) {
        return world.getEntityByID(this.entityId);
    }
}
