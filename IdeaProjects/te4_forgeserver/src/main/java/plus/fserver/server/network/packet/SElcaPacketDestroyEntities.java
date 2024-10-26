package plus.fserver.server.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import plus.fserver.server.network.NetUtils;
import plus.fserver.utls.collections.Int1LinkedList;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class SElcaPacketDestroyEntities extends SPacketPlayBase {
    private final Int1LinkedList.Node nodes;

    public SElcaPacketDestroyEntities(){
        this.nodes = new Int1LinkedList.Node(null,0);
    }

    public SElcaPacketDestroyEntities(Int1LinkedList.Node nodes) {
        this.nodes = nodes;
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        throw new UnsupportedEncodingException("THIS PACKET WRITE-ONLY! USE [SPacketDestroyEntities]");
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        int len = 1;
        Int1LinkedList.Node node = this.nodes, temp = node;

        while (temp != null){
            len += 1;
            temp = temp.getParent();
        }

        buf.writeVarInt(len);

        while (node != null){
            buf.writeVarInt(node.getValue());
            node = node.getParent();
        }
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        int len = 0;
        Int1LinkedList.Node node = this.nodes, temp = node;

        while (temp != null){
            len += 1;
            temp = temp.getParent();
        }

        NetUtils.writeVarInt(buf, len);

        while (node != null){
            NetUtils.writeVarInt(buf, node.getValue());
            node = node.getParent();
        }
    }


    @Override
    public void processPacket(INetHandlerPlayClient client) {
        int i = 0;
        Int1LinkedList.Node node = this.nodes, temp = node;

        while (temp != null){
            i += 1;
            temp = temp.getParent();
        }

        int[] ids = new int[i];
        i = 0;

        while (node != null){
            ids[i] = node.getValue();
            node = node.getParent();
        }
        client.handleDestroyEntities(new SPacketDestroyEntities(ids));
    }
}