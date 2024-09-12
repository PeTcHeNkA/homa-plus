package net.minecraft.network.play.server;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SPacketPlayBase;


public class SPacketStatistics extends SPacketPlayBase {
    private Map<StatBase, Integer> statisticMap;

    public SPacketStatistics() {}

    public SPacketStatistics(Map<StatBase, Integer> map) {
        this.statisticMap = map;
    }


    @Override
    public void processPacket(INetHandlerPlayClient proc) {
        proc.handleStatistics(this);
    }


    @Deprecated
    public void readPacketData(PacketBuffer buf) throws IOException {
        int size = buf.readVarInt();
        this.statisticMap = Maps.newHashMap();

        for(int i = 0; i < size; ++i) {
            StatBase id = StatList.getOneShotStat(buf.readString(32767));
            int value = buf.readVarInt();
            if (id != null) {
                this.statisticMap.put(id, value);
            }
        }
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.statisticMap.size());

        for (Map.Entry<StatBase, Integer> entry : this.statisticMap.entrySet()) {
            buf.writeString(entry.getKey().statId);
            buf.writeVarInt(entry.getValue());
        }
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        for (Map.Entry<StatBase, Integer> entry : this.statisticMap.entrySet()) {
            NetUtils.writeString(buf, entry.getKey().statId);
            NetUtils.writeVarInt(buf, entry.getValue());
        }
    }


    @SideOnly(Side.CLIENT)
    public Map<StatBase, Integer> getStatisticMap() {
        return this.statisticMap;
    }
}