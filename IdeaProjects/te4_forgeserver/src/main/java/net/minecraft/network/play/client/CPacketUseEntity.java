package net.minecraft.network.play.client;

import java.io.IOException;
import javax.annotation.Nullable;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.CPacketPlayBase;


public class CPacketUseEntity extends CPacketPlayBase {
    private int entityId;
    private Action action;
    private Vec3d hitVec;
    private EnumHand hand;

    public CPacketUseEntity() {}

    public CPacketUseEntity(Entity entity) {
        this.entityId = entity.getEntityId();
        this.action = CPacketUseEntity.Action.ATTACK;
    }

    @SideOnly(Side.CLIENT)
    public CPacketUseEntity(Entity entity, EnumHand hand) {
        this.entityId = entity.getEntityId();
        this.action = CPacketUseEntity.Action.INTERACT;
        this.hand = hand;
    }

    @SideOnly(Side.CLIENT)
    public CPacketUseEntity(Entity entity, EnumHand hand, Vec3d hitVec) {
        this.entityId = entity.getEntityId();
        this.action = CPacketUseEntity.Action.INTERACT_AT;
        this.hand = hand;
        this.hitVec = hitVec;
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityId = buf.readVarInt();
        this.action = buf.readEnumValue(Action.class);
        if (this.action == CPacketUseEntity.Action.INTERACT_AT) {
            this.hitVec = new Vec3d(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }

        if (this.action == CPacketUseEntity.Action.INTERACT || this.action == CPacketUseEntity.Action.INTERACT_AT) {
            this.hand = buf.readEnumValue(EnumHand.class);
        }
    }


    @Override
    public void readPacketData(ByteBuf buf) throws IOException {
        this.entityId = NetUtils.readVarInt(buf);
        this.action = NetUtils.readEnumValueFast(buf, Action.VALUES);
        if (this.action == CPacketUseEntity.Action.INTERACT_AT) {
            this.hitVec = new Vec3d(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }
        if (this.action == CPacketUseEntity.Action.INTERACT || this.action == CPacketUseEntity.Action.INTERACT_AT) {
            this.hand = NetUtils.readEnumValueFast(buf, EnumHand.VALUES);
        }
    }


    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.entityId);
        buf.writeEnumValue(this.action);
        if (this.action == CPacketUseEntity.Action.INTERACT_AT) {
            buf.writeFloat((float)this.hitVec.x);
            buf.writeFloat((float)this.hitVec.y);
            buf.writeFloat((float)this.hitVec.z);
        }

        if (this.action == CPacketUseEntity.Action.INTERACT || this.action == CPacketUseEntity.Action.INTERACT_AT) {
            buf.writeEnumValue(this.hand);
        }
    }


    @Override
    public void processPacket(INetHandlerPlayServer proc) {
        proc.processUseEntity(this);
    }


    @Nullable
    public Entity getEntityFromWorld(World world) {
        return world.getEntityByID(this.entityId);
    }


    public Action getAction() {
        return this.action;
    }


    public EnumHand getHand() {
        return this.hand;
    }


    public Vec3d getHitVec() {
        return this.hitVec;
    }


    public static enum Action {
        INTERACT,
        ATTACK,
        INTERACT_AT;

        private Action() {}

        public static final Action[] VALUES = values();
    }
}