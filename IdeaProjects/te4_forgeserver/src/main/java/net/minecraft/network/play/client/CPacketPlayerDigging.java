package net.minecraft.network.play.client;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;


public class CPacketPlayerDigging implements Packet<INetHandlerPlayServer> {
    private BlockPos position;
    private EnumFacing facing;
    private Action action;

    public CPacketPlayerDigging() {}

    @SideOnly(Side.CLIENT)
    public CPacketPlayerDigging(Action action, BlockPos pos, EnumFacing facing) {
        this.action = action;
        this.position = pos;
        this.facing = facing;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.action = buf.readEnumValue(Action.class);
        this.position = buf.readBlockPos();
        this.facing = EnumFacing.byIndex(buf.readUnsignedByte());
    }


    @Override
    public void readPacketData(ByteBuf buf) throws IOException {
        this.action = NetUtils.readEnumValue(buf, Action.class);
        this.position = NetUtils.readBlockPos(buf);
        this.facing = EnumFacing.byIndex(buf.readUnsignedByte());
    }


    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(this.action);
        buf.writeBlockPos(this.position);
        buf.writeByte(this.facing.getIndex());
    }


    @Override
    public void processPacket(INetHandlerPlayServer proc) {
        proc.processPlayerDigging(this);
    }


    public BlockPos getPosition() {
        return this.position;
    }


    public EnumFacing getFacing() {
        return this.facing;
    }


    public Action getAction() {
        return this.action;
    }


    public static enum Action {
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM,
        SWAP_HELD_ITEMS;

        private Action() {
        }
    }
}