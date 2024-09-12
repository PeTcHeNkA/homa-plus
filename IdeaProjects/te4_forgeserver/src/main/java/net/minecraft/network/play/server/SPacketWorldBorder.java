package net.minecraft.network.play.server;

import java.io.IOException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketWorldBorder extends SPacketPlayBase {
    private Action action;
    private int size;
    private double centerX;
    private double centerZ;
    private double targetSize;
    private double diameter;
    private long timeUntilTarget;
    private int warningTime;
    private int warningDistance;

    public SPacketWorldBorder() {}

    public SPacketWorldBorder(WorldBorder border, Action action) {
        this.action = action;
        this.centerX = border.getCenterX();
        this.centerZ = border.getCenterZ();
        this.diameter = border.getDiameter();
        this.targetSize = border.getTargetSize();
        this.timeUntilTarget = border.getTimeUntilTarget();
        this.size = border.getSize();
        this.warningDistance = border.getWarningDistance();
        this.warningTime = border.getWarningTime();
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.action = buf.readEnumValue(Action.class);
        switch (this.action) {
            case SET_SIZE:
                this.targetSize = buf.readDouble();
                break;
            case LERP_SIZE:
                this.diameter = buf.readDouble();
                this.targetSize = buf.readDouble();
                this.timeUntilTarget = buf.readVarLong();
                break;
            case SET_CENTER:
                this.centerX = buf.readDouble();
                this.centerZ = buf.readDouble();
                break;
            case SET_WARNING_BLOCKS:
                this.warningDistance = buf.readVarInt();
                break;
            case SET_WARNING_TIME:
                this.warningTime = buf.readVarInt();
                break;
            case INITIALIZE:
                this.centerX = buf.readDouble();
                this.centerZ = buf.readDouble();
                this.diameter = buf.readDouble();
                this.targetSize = buf.readDouble();
                this.timeUntilTarget = buf.readVarLong();
                this.size = buf.readVarInt();
                this.warningDistance = buf.readVarInt();
                this.warningTime = buf.readVarInt();
        }

    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        writePacketDataRaw(buf);
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);
        writePacketDataRaw(buf);
    }


    private void writePacketDataRaw(ByteBuf buf) {
        NetUtils.writeEnumValue(buf, this.action);
        switch (this.action) {
            case SET_SIZE:
                buf.writeDouble(this.targetSize);
                break;
            case LERP_SIZE:
                buf.writeDouble(this.diameter);
                buf.writeDouble(this.targetSize);
                NetUtils.writeVarLong(buf, this.timeUntilTarget);
                break;
            case SET_CENTER:
                buf.writeDouble(this.centerX);
                buf.writeDouble(this.centerZ);
                break;
            case SET_WARNING_BLOCKS:
                NetUtils.writeVarInt(buf, this.warningDistance);
                break;
            case SET_WARNING_TIME:
                NetUtils.writeVarInt(buf, this.warningTime);
                break;
            case INITIALIZE:
                buf.writeDouble(this.centerX);
                buf.writeDouble(this.centerZ);
                buf.writeDouble(this.diameter);
                buf.writeDouble(this.targetSize);
                NetUtils.writeVarLong(buf, this.timeUntilTarget);
                NetUtils.writeVarInt(buf, this.size);
                NetUtils.writeVarInt(buf, this.warningDistance);
                NetUtils.writeVarInt(buf, this.warningTime);
        }
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleWorldBorder(this);
    }


    @SideOnly(Side.CLIENT)
    public void apply(WorldBorder border) {
        switch (this.action) {
            case SET_SIZE:
                border.setTransition(this.targetSize);
                break;
            case LERP_SIZE:
                border.setTransition(this.diameter, this.targetSize, this.timeUntilTarget);
                break;
            case SET_CENTER:
                border.setCenter(this.centerX, this.centerZ);
                break;
            case SET_WARNING_BLOCKS:
                border.setWarningDistance(this.warningDistance);
                break;
            case SET_WARNING_TIME:
                border.setWarningTime(this.warningTime);
                break;
            case INITIALIZE:
                border.setCenter(this.centerX, this.centerZ);
                if (this.timeUntilTarget > 0L) {
                    border.setTransition(this.diameter, this.targetSize, this.timeUntilTarget);
                } else {
                    border.setTransition(this.targetSize);
                }

                border.setSize(this.size);
                border.setWarningDistance(this.warningDistance);
                border.setWarningTime(this.warningTime);
        }

    }


    public enum Action {
        SET_SIZE,
        LERP_SIZE,
        SET_CENTER,
        INITIALIZE,
        SET_WARNING_TIME,
        SET_WARNING_BLOCKS;

        Action() {}
    }
}