package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team.CollisionRule;
import net.minecraft.scoreboard.Team.EnumVisible;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketTeams extends SPacketPlayBase {
    private String name = "";
    private String displayName = "";
    private String prefix = "";
    private String suffix = "";
    private String nameTagVisibility;
    private String collisionRule;
    private int color;
    private final Collection<String> players;
    private int action;
    private int friendlyFlags;

    public SPacketTeams() {
        this.nameTagVisibility = EnumVisible.ALWAYS.internalName;
        this.collisionRule = CollisionRule.ALWAYS.name;
        this.color = -1;
        this.players = Lists.newArrayList();
    }


    public SPacketTeams(ScorePlayerTeam p_i46907_1_, int p_i46907_2_) {
        this.nameTagVisibility = EnumVisible.ALWAYS.internalName;
        this.collisionRule = CollisionRule.ALWAYS.name;
        this.color = -1;
        this.players = Lists.newArrayList();
        this.name = p_i46907_1_.getName();
        this.action = p_i46907_2_;
        if (p_i46907_2_ == 0 || p_i46907_2_ == 2) {
            this.displayName = p_i46907_1_.getDisplayName();
            this.prefix = p_i46907_1_.getPrefix();
            this.suffix = p_i46907_1_.getSuffix();
            this.friendlyFlags = p_i46907_1_.getFriendlyFlags();
            this.nameTagVisibility = p_i46907_1_.getNameTagVisibility().internalName;
            this.collisionRule = p_i46907_1_.getCollisionRule().name;
            this.color = p_i46907_1_.getColor().getColorIndex();
        }
        if (p_i46907_2_ == 0) {
            this.players.addAll(p_i46907_1_.getMembershipCollection());
        }

    }


    public SPacketTeams(ScorePlayerTeam p_i46908_1_, Collection<String> p_i46908_2_, int p_i46908_3_) {
        this.nameTagVisibility = EnumVisible.ALWAYS.internalName;
        this.collisionRule = CollisionRule.ALWAYS.name;
        this.color = -1;
        this.players = Lists.newArrayList();
        if (p_i46908_3_ != 3 && p_i46908_3_ != 4) {
            throw new IllegalArgumentException("Method must be join or leave for player constructor");
        } else if (p_i46908_2_ != null && !p_i46908_2_.isEmpty()) {
            this.action = p_i46908_3_;
            this.name = p_i46908_1_.getName();
            this.players.addAll(p_i46908_2_);
        } else {
            throw new IllegalArgumentException("Players cannot be null/empty");
        }
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.name = buf.readString(16);
        this.action = buf.readByte();
        if (this.action == 0 || this.action == 2) {
            this.displayName = buf.readString(32);
            this.prefix = buf.readString(16);
            this.suffix = buf.readString(16);
            this.friendlyFlags = buf.readByte();
            this.nameTagVisibility = buf.readString(32);
            this.collisionRule = buf.readString(32);
            this.color = buf.readByte();
        }

        if (this.action == 0 || this.action == 3 || this.action == 4) {
            int lvt_2_1_ = buf.readVarInt();

            for(int lvt_3_1_ = 0; lvt_3_1_ < lvt_2_1_; ++lvt_3_1_) {
                this.players.add(buf.readString(40));
            }
        }

    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeString(this.name);
        buf.writeByte(this.action);
        if (this.action == 0 || this.action == 2) {
            buf.writeString(this.displayName);
            buf.writeString(this.prefix);
            buf.writeString(this.suffix);
            buf.writeByte(this.friendlyFlags);
            buf.writeString(this.nameTagVisibility);
            buf.writeString(this.collisionRule);
            buf.writeByte(this.color);
        }

        if (this.action == 0 || this.action == 3 || this.action == 4) {
            buf.writeVarInt(this.players.size());

            for (String lvt_3_1_ : this.players) {
                buf.writeString(lvt_3_1_);
            }
        }
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);
        NetUtils.writeString(buf, this.name);
        int action;
        buf.writeByte(action = this.action);
        if (action == 0 || action == 2) {
            NetUtils.writeString(buf, this.displayName);
            NetUtils.writeString(buf, this.prefix);
            NetUtils.writeString(buf, this.suffix);
            buf.writeByte(this.friendlyFlags);
            NetUtils.writeString(buf, this.nameTagVisibility);
            NetUtils.writeString(buf, this.collisionRule);
            buf.writeByte(this.color);
        }
        if (action == 0 || action == 3 || action == 4) {
            Collection<String> players;
            NetUtils.writeVarInt(buf, (players = this.players).size());
            for (String str : players) NetUtils.writeString(buf, str);
        }
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleTeams(this);
    }


    @SideOnly(Side.CLIENT)
    public String getName() {
        return this.name;
    }


    @SideOnly(Side.CLIENT)
    public String getDisplayName() {
        return this.displayName;
    }


    @SideOnly(Side.CLIENT)
    public String getPrefix() {
        return this.prefix;
    }


    @SideOnly(Side.CLIENT)
    public String getSuffix() {
        return this.suffix;
    }


    @SideOnly(Side.CLIENT)
    public Collection<String> getPlayers() {
        return this.players;
    }


    @SideOnly(Side.CLIENT)
    public int getAction() {
        return this.action;
    }


    @SideOnly(Side.CLIENT)
    public int getFriendlyFlags() {
        return this.friendlyFlags;
    }


    @SideOnly(Side.CLIENT)
    public int getColor() {
        return this.color;
    }


    @SideOnly(Side.CLIENT)
    public String getNameTagVisibility() {
        return this.nameTagVisibility;
    }


    @SideOnly(Side.CLIENT)
    public String getCollisionRule() {
        return this.collisionRule;
    }
}
