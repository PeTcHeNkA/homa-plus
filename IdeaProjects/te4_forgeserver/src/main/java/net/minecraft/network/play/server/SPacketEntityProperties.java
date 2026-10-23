package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SEntityPacketPlayBase;


public class SPacketEntityProperties extends SEntityPacketPlayBase {
    private int entityId;
    private final List<Snapshot> snapshots;

    public SPacketEntityProperties() {
        snapshots = new ArrayList<>();
    }


    public SPacketEntityProperties(int id, Collection<IAttributeInstance> attributes) {
        this.entityId = id;
        snapshots = new ArrayList<>(attributes.size());
        Iterator var3 = attributes.iterator();

        while(var3.hasNext()) {
            IAttributeInstance inst = (IAttributeInstance)var3.next();
            this.snapshots.add(new Snapshot(inst.getAttribute().getName(), inst.getBaseValue(), inst.getModifiers()));
        }
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityId = buf.readVarInt();
        int count = buf.readInt();

        for(int i = 0; i < count; ++i) {
            String name = buf.readString(64);
            double lvt_5_1_ = buf.readDouble();
            List<AttributeModifier> lvt_7_1_ = Lists.newArrayList();
            int lvt_8_1_ = buf.readVarInt();

            for(int lvt_9_1_ = 0; lvt_9_1_ < lvt_8_1_; ++lvt_9_1_) {
                UUID lvt_10_1_ = buf.readUniqueId();
                lvt_7_1_.add(new AttributeModifier(lvt_10_1_, "Unknown synced attribute modifier", buf.readDouble(), buf.readByte()));
            }
            this.snapshots.add(new Snapshot(name, lvt_5_1_, lvt_7_1_));
        }
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.entityId);
        buf.writeInt(this.snapshots.size());
        Iterator var2 = this.snapshots.iterator();

        while(var2.hasNext()) {
            Snapshot lvt_3_1_ = (Snapshot)var2.next();
            buf.writeString(lvt_3_1_.getName());
            buf.writeDouble(lvt_3_1_.getBaseValue());
            buf.writeVarInt(lvt_3_1_.getModifiers().size());
            Iterator var4 = lvt_3_1_.getModifiers().iterator();

            while(var4.hasNext()) {
                AttributeModifier lvt_5_1_ = (AttributeModifier)var4.next();
                buf.writeUniqueId(lvt_5_1_.getID());
                buf.writeDouble(lvt_5_1_.getAmount());
                buf.writeByte(lvt_5_1_.getOperation());
            }
        }
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        NetUtils.writeVarInt(buf, this.entityId);
        buf.writeInt(this.snapshots.size());
        Iterator var2 = this.snapshots.iterator();

        while(var2.hasNext()) {
            Snapshot snapshot = (Snapshot)var2.next();
            NetUtils.writeString(buf, snapshot.getName());
            buf.writeDouble(snapshot.getBaseValue());
            NetUtils.writeVarInt(buf, snapshot.getModifiers().size());
            Iterator var4 = snapshot.getModifiers().iterator();

            while(var4.hasNext()) {
                AttributeModifier modifier = (AttributeModifier)var4.next();
                NetUtils.writeUniqueId(buf, modifier.getID());
                buf.writeDouble(modifier.getAmount());
                buf.writeByte(modifier.getOperation());
            }
        }
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleEntityProperties(this);
    }


    @SideOnly(Side.CLIENT)
    public int getEntityId() {
        return this.entityId;
    }


    @SideOnly(Side.CLIENT)
    public List<Snapshot> getSnapshots() {
        return this.snapshots;
    }


    public static class Snapshot {
        private final String name;
        private final double baseValue;
        private final Collection<AttributeModifier> modifiers;

        public Snapshot(String name, double value, Collection<AttributeModifier> modifiers) {
            this.name = name;
            this.baseValue = value;
            this.modifiers = modifiers;
        }


        public String getName() {
            return this.name;
        }


        public double getBaseValue() {
            return this.baseValue;
        }


        public Collection<AttributeModifier> getModifiers() {
            return this.modifiers;
        }
    }
}