package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketSpawnPosition extends SPacketPlayBase {
    private BlockPos spawnBlockPos;

    public SPacketSpawnPosition() {}

    public SPacketSpawnPosition(BlockPos pos) {
        this.spawnBlockPos = pos;
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.spawnBlockPos = buf.readBlockPos();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeBlockPos(this.spawnBlockPos);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);
        NetUtils.writeBlockPos(buf, this.spawnBlockPos);
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleSpawnPosition(this);
    }


    @SideOnly(Side.CLIENT)
    public BlockPos getSpawnPos() {
        return this.spawnBlockPos;
    }
}