package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.List;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.packet.SEntityPacketPlayBase;


public class SPacketEntityMetadata extends SEntityPacketPlayBase {
    private int entityId;
    private List<EntityDataManager.DataEntry<?>> dataManagerEntries;

    public SPacketEntityMetadata() {}

    public SPacketEntityMetadata(int id, List<EntityDataManager.DataEntry<?>> entries){
        this.entityId = id;
        this.dataManagerEntries = entries;
    }

    public SPacketEntityMetadata(int id, EntityDataManager manager, boolean all) {
        this.entityId = id;
        if (all) {
            this.dataManagerEntries = manager.getAll();
            manager.setClean();
        } else {
            this.dataManagerEntries = manager.getDirty();
        }
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityId = buf.readVarInt();
        this.dataManagerEntries = EntityDataManager.readEntries(buf);
    }


    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.entityId);
        EntityDataManager.writeEntries(this.dataManagerEntries, buf);
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleEntityMetadata(this);
    }


    @SideOnly(Side.CLIENT)
    public List<EntityDataManager.DataEntry<?>> getDataManagerEntries() {
        return this.dataManagerEntries;
    }


    @SideOnly(Side.CLIENT)
    public int getEntityId() {
        return this.entityId;
    }
}
