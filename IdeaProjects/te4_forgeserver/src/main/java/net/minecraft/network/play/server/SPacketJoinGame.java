package net.minecraft.network.play.server;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SNodePacketPlayBase;


public class SPacketJoinGame extends SNodePacketPlayBase {
    private int playerId;
    private boolean hardcoreMode;
    private GameType gameType;
    private int dimension;
    private EnumDifficulty difficulty;
    private int maxPlayers;
    private WorldType worldType;
    private boolean reducedDebugInfo;

    public SPacketJoinGame() {}

    public SPacketJoinGame(int id, GameType type, boolean hardcore, int dim, EnumDifficulty diff, int maxPlayers, WorldType worldType, boolean debug) {
        this.playerId = id;
        this.dimension = dim;
        this.difficulty = diff;
        this.gameType = type;
        this.maxPlayers = maxPlayers;
        this.hardcoreMode = hardcore;
        this.worldType = worldType;
        this.reducedDebugInfo = debug;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.playerId = buf.readInt();
        int lvt_2_1_ = buf.readUnsignedByte();
        this.hardcoreMode = (lvt_2_1_ & 8) == 8;
        lvt_2_1_ &= -9;
        this.gameType = GameType.getByID(lvt_2_1_);
        this.dimension = buf.readInt();
        this.difficulty = EnumDifficulty.byId(buf.readUnsignedByte());
        this.maxPlayers = buf.readUnsignedByte();
        this.worldType = WorldType.byName(buf.readString(16));
        if (this.worldType == null) {
            this.worldType = WorldType.DEFAULT;
        }
        this.reducedDebugInfo = buf.readBoolean();
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeInt(this.playerId);
        int flags = this.gameType.getID();
        if (this.hardcoreMode) {
            flags |= 8;
        }

        buf.writeByte(flags);
        buf.writeInt(this.dimension);
        buf.writeByte(this.difficulty.getId());
        buf.writeByte(this.maxPlayers);
        buf.writeString(this.worldType.getName());
        buf.writeBoolean(this.reducedDebugInfo);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        buf.writeInt(this.playerId);
        int flags = this.gameType.getID();
        if (this.hardcoreMode) {
            flags |= 8;
        }

        buf.writeByte(flags);
        buf.writeInt(this.dimension);
        buf.writeByte(this.difficulty.getId());
        buf.writeByte(this.maxPlayers);
        NetUtils.writeString(buf, this.worldType.getName());
        buf.writeBoolean(this.reducedDebugInfo);
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleJoinGame(this);
    }


    @SideOnly(Side.CLIENT)
    public int getPlayerId() {
        return this.playerId;
    }


    @SideOnly(Side.CLIENT)
    public boolean isHardcoreMode() {
        return this.hardcoreMode;
    }


    @SideOnly(Side.CLIENT)
    public GameType getGameType() {
        return this.gameType;
    }


    @SideOnly(Side.CLIENT)
    public int getDimension() {
        return this.dimension;
    }


    @SideOnly(Side.CLIENT)
    public EnumDifficulty getDifficulty() {
        return this.difficulty;
    }


    @SideOnly(Side.CLIENT)
    public int getMaxPlayers() {
        return this.maxPlayers;
    }


    @SideOnly(Side.CLIENT)
    public WorldType getWorldType() {
        return this.worldType;
    }


    @SideOnly(Side.CLIENT)
    public boolean isReducedDebugInfo() {
        return this.reducedDebugInfo;
    }
}
