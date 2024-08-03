package net.minecraft.network.play.server;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import plus.fserver.server.network.NetUtils;


public class SPacketCombatEvent implements Packet<INetHandlerPlayClient> {
    public Event eventType;
    public int playerId;
    public int entityId;
    public int duration;
    public ITextComponent deathMessage;

    public SPacketCombatEvent() {}

    public SPacketCombatEvent(CombatTracker tracker, Event event) {
        this(tracker, event, true);
    }


    public SPacketCombatEvent(CombatTracker tracker, Event event, boolean flag) {
        this.eventType = event;
        EntityLivingBase attacker = tracker.getBestAttacker();
        switch (event) {
            case END_COMBAT:
                this.duration = tracker.getCombatDuration();
                this.entityId = attacker == null ? -1 : attacker.getEntityId();
                break;
            case ENTITY_DIED:
                this.playerId = tracker.getFighter().getEntityId();
                this.entityId = attacker == null ? -1 : attacker.getEntityId();
                if (flag) {
                    this.deathMessage = tracker.getDeathMessage();
                } else {
                    this.deathMessage = new TextComponentString("");
                }
        }
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.eventType = buf.readEnumValue(Event.class);
        if (this.eventType == SPacketCombatEvent.Event.END_COMBAT) {
            this.duration = buf.readVarInt();
            this.entityId = buf.readInt();
        } else if (this.eventType == SPacketCombatEvent.Event.ENTITY_DIED) {
            this.playerId = buf.readVarInt();
            this.entityId = buf.readInt();
            this.deathMessage = buf.readTextComponent();
        }
    }


    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(this.eventType);
        if (this.eventType == SPacketCombatEvent.Event.END_COMBAT) {
            buf.writeVarInt(this.duration);
            buf.writeInt(this.entityId);
        } else if (this.eventType == SPacketCombatEvent.Event.ENTITY_DIED) {
            buf.writeVarInt(this.playerId);
            buf.writeInt(this.entityId);
            buf.writeTextComponent(this.deathMessage);
        }
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeEnumValue(buf, this.eventType);
        if (this.eventType == SPacketCombatEvent.Event.END_COMBAT) {
            NetUtils.writeVarInt(buf, this.duration);
            buf.writeInt(this.entityId);
        } else if (this.eventType == SPacketCombatEvent.Event.ENTITY_DIED) {
            NetUtils.writeVarInt(buf, this.playerId);
            buf.writeInt(this.entityId);
            NetUtils.writeTextComponent(buf, this.deathMessage);
        }
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        client.handleCombatEvent(this);
    }


    public static enum Event {
        ENTER_COMBAT,
        END_COMBAT,
        ENTITY_DIED;

        private Event() {}
    }
}