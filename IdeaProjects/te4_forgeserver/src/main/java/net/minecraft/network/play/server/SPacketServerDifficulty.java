package net.minecraft.network.play.server;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SNodePacketPlayBase;


public class SPacketServerDifficulty extends SNodePacketPlayBase {
    private EnumDifficulty difficulty;
    private boolean difficultyLocked;

    public SPacketServerDifficulty() {}


    public SPacketServerDifficulty(EnumDifficulty difficulty, boolean locked) {
        this.difficulty = difficulty;
        this.difficultyLocked = locked;
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleServerDifficulty(this);
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.difficulty = EnumDifficulty.byId(buf.readUnsignedByte());
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);
        buf.writeByte(this.difficulty.getId());
    }


    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeByte(this.difficulty.getId());
    }


    @SideOnly(Side.CLIENT)
    public boolean isDifficultyLocked() {
        return this.difficultyLocked;
    }


    @SideOnly(Side.CLIENT)
    public EnumDifficulty getDifficulty() {
        return this.difficulty;
    }
}